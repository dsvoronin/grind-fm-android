package com.dsvoronin.grindfm;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import com.dsvoronin.grindfm.adapter.NewsAdapter;
import com.dsvoronin.grindfm.adapter.RequestSongAdapter;
import com.dsvoronin.grindfm.adapter.VideoAdapter;
import com.dsvoronin.grindfm.task.NewsTask;
import com.dsvoronin.grindfm.task.RequestSearchTask;
import com.dsvoronin.grindfm.task.RequestTask;
import com.dsvoronin.grindfm.task.VideoTask;
import com.dsvoronin.grindfm.view.GesturableViewFlipper;
import com.dsvoronin.grindfm.view.MenuButton;

import java.util.HashMap;
import java.util.Map;

public class GrindActivity extends Activity implements GesturableViewFlipper.OnSwitchListener, MenuButton.Pickable, ServiceConnection {

    private static final int MENU_ID_RADIO = 0;
    private static final int MENU_ID_NEWS = 1;
    private static final int MENU_ID_VIDEO = 2;
    private static final int MENU_ID_VKONTAKTE = 3;
    private static final int MENU_ID_REQUEST = 4;

    private Map<Integer, MenuButton> menuButtons = new HashMap<Integer, MenuButton>();

    //flags
    private boolean newsFirstStart = true;
    private boolean videoFirstStart = true;

    //control
    private ImageView playPause;
    private ProgressBar progressBar;
    private RelativeLayout radioControl;

    //flipper
    private GesturableViewFlipper flipper;

    //lists
    private ListView newsList;
    private ListView videoList;
    private ListView requestList;

    //progress
    private ImageView newsProgress;
    private ImageView videoProgress;

    private Button newsTryAgain;
    private Button videoTryAgain;

    //header string
    private TextView headerRunningString;

    //web
    private WebView vkontakteWebView;

    //adapters
    private NewsAdapter newsAdapter;
    private VideoAdapter videoAdapter;
    private RequestSongAdapter requestSongAdapter;

    //recievers
    private GrindReceiver grindReceiver;

    private IGrindPlayer.Stub binder = null;

    //request
    private Button searchButton;
    private EditText searchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.grind);
        initViews();

        //щелкаем по первому пункту меню
        menuButtons.get(0).performClick();

        vkontakteWebView.setWebViewClient(new VkontakteWebViewClient());

        //включает бегущую строку
        headerRunningString.setSelected(true);

        newsAdapter = new NewsAdapter(this);
        newsList.setAdapter(newsAdapter);

        videoAdapter = new VideoAdapter(this);
        videoList.setAdapter(videoAdapter);
        videoList.setOnItemClickListener(onVideoItemClickListener);

        requestSongAdapter = new RequestSongAdapter(this);
        requestList.setAdapter(requestSongAdapter);
        requestList.setOnItemClickListener(onSongClickListener);

        flipper.setOnSwitchListener(this);
    }

    @Override
    protected void onResume() {
        grindReceiver = new GrindReceiver();
        registerReceiver(grindReceiver, new IntentFilter(getString(R.string.service_intent)));

        if (!isMyServiceRunning()) {
            initStream();
        } else {
            startStream();
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(grindReceiver);
        super.onPause();
    }

    @Override
    public void onSwitch(int childId) {
        menuButtons.get(childId).performClick();
    }

    private void performSwitch(int childId) {
        if (flipper.getDisplayedChild() > childId) {
            flipper.setInAnimation(GrindActivity.this, R.anim.in_from_left);
            flipper.setOutAnimation(GrindActivity.this, R.anim.out_to_right);
            flipper.setDisplayedChild(childId);
        } else if (flipper.getDisplayedChild() < childId) {
            flipper.setInAnimation(GrindActivity.this, R.anim.in_from_right);
            flipper.setOutAnimation(GrindActivity.this, R.anim.out_to_left);
            flipper.setDisplayedChild(childId);
        } else {
            //do nothing
        }
    }

    private void initViews() {
        vkontakteWebView = (WebView) findViewById(R.id.vkontakteWebView);
        headerRunningString = (TextView) findViewById(R.id.header_running_string);

        //lists
        newsList = (ListView) findViewById(R.id.newsList);
        videoList = (ListView) findViewById(R.id.videoList);
        requestList = (ListView) findViewById(R.id.request_list);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        playPause = (ImageView) findViewById(R.id.play_pause);
        flipper = (GesturableViewFlipper) findViewById(R.id.flipper);

        //progress
        newsProgress = (ImageView) findViewById(R.id.news_progress);
        videoProgress = (ImageView) findViewById(R.id.video_progress);

        newsTryAgain = (Button) findViewById(R.id.news_try_again);
        newsTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewsTask task = new NewsTask(GrindActivity.this, newsAdapter);
                task.setProgress(newsProgress);
                task.setTryAgain(newsTryAgain);
                task.execute(getString(R.string.news_url));
            }
        });
        videoTryAgain = (Button) findViewById(R.id.video_try_again);
        videoTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VideoTask task = new VideoTask(GrindActivity.this, videoAdapter);
                task.setProgress(videoProgress);
                task.setTryAgain(videoTryAgain);
                task.execute(getString(R.string.youtube_playlist_postrelushka));
            }
        });

        radioControl = (RelativeLayout) findViewById(R.id.radio_contol);

        MenuButton radioButton = (MenuButton) findViewById(R.id.menu_radio);
        radioButton.setOnPickListener(new MenuButton.OnPickListener() {
            @Override
            public void onPick() {
                performSwitch(MENU_ID_RADIO);
            }
        });
        menuButtons.put(MENU_ID_RADIO, radioButton);

        MenuButton newsButton = (MenuButton) findViewById(R.id.menu_news);
        newsButton.setOnPickListener(new MenuButton.OnPickListener() {
            @Override
            public void onPick() {
                if (newsFirstStart) {
                    NewsTask task = new NewsTask(GrindActivity.this, newsAdapter);
                    task.setProgress(newsProgress);
                    task.setTryAgain(newsTryAgain);
                    task.execute(getString(R.string.news_url));
                    newsFirstStart = false;
                }
                performSwitch(MENU_ID_NEWS);
            }
        });
        menuButtons.put(MENU_ID_NEWS, newsButton);

        MenuButton videoButton = (MenuButton) findViewById(R.id.menu_video);
        videoButton.setOnPickListener(new MenuButton.OnPickListener() {
            @Override
            public void onPick() {
                if (videoFirstStart) {
                    VideoTask task = new VideoTask(GrindActivity.this, videoAdapter);
                    task.setProgress(videoProgress);
                    task.setTryAgain(videoTryAgain);
                    task.execute(getString(R.string.youtube_playlist_postrelushka));
                    videoFirstStart = false;
                }
                performSwitch(MENU_ID_VIDEO);
            }
        });
        menuButtons.put(MENU_ID_VIDEO, videoButton);

        MenuButton vkButton = (MenuButton) findViewById(R.id.menu_vk);
        vkButton.setOnPickListener(new MenuButton.OnPickListener() {
            @Override
            public void onPick() {
                vkontakteWebView.loadUrl(getString(R.string.vkontakte_url));
                vkontakteWebView.requestFocus(View.FOCUS_DOWN);
                performSwitch(MENU_ID_VKONTAKTE);
            }
        });
        menuButtons.put(MENU_ID_VKONTAKTE, vkButton);

        MenuButton requestButton = (MenuButton) findViewById(R.id.menu_request);
        requestButton.setOnPickListener(new MenuButton.OnPickListener() {
            @Override
            public void onPick() {
                performSwitch(MENU_ID_REQUEST);
            }
        });
        menuButtons.put(MENU_ID_REQUEST, requestButton);

        searchText = (EditText) findViewById(R.id.search_query);

        searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestSearchTask task = new RequestSearchTask(GrindActivity.this, requestSongAdapter);
                task.execute(searchText.getText().toString());

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
            }
        });
    }

    @Override
    public void pick(MenuButton pickedButton) {
        for (MenuButton button : menuButtons.values()) {
            if (!button.equals(pickedButton)) {
                button.setUnpicked();
            }
        }
    }

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
        String TAG = "OnServiceConnected";
        Log.d(TAG, "START");

        binder = (IGrindPlayer.Stub) service;

        try {
            Log.d(TAG, "Is service playing audio? " + binder.playing());

            startService(new Intent(this, GrindService.class));
        } catch (RemoteException e) {
            Log.e(TAG, "onServiceConnected", e);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        String TAG = "OnServiceDisconnected";
        Log.d(TAG, "START");
        Log.d(TAG, "Binder = null");
        binder = null;
    }

    private class GrindReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String info = intent.getStringExtra(getString(R.string.service_intent_info));
            headerRunningString.setText(info);
            startStream();
        }
    }

    private void initStream() {
        playPause.setImageResource(android.R.drawable.ic_media_play);
        radioControl.setOnClickListener(playClickListener);
        playPause.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void prepareStream() {
        radioControl.setOnClickListener(null);
        playPause.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void startStream() {
        radioControl.setOnClickListener(stopClickListener);
        playPause.setImageResource(android.R.drawable.ic_media_pause);
        playPause.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (GrindService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private class VkontakteWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    /**
     * Отправляем линк на ютуб видео внешней программе
     */
    private AdapterView.OnItemClickListener onVideoItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + videoAdapter.getItem(i).getUrl())));
        }
    };

    private AdapterView.OnItemClickListener onSongClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            RequestTask task = new RequestTask(GrindActivity.this);
            task.execute(requestSongAdapter.getItem(i).getId());
        }
    };

    /**
     * Запускаем стрим по клику на плей
     */
    private View.OnClickListener playClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String TAG = "PlayClick";

            Intent i = new Intent(GrindActivity.this, GrindService.class);
            try {
                if (binder != null && binder.playing()) {
                    binder.stopAudio();
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Cant stop");
            }
            Log.d(TAG, "Bind to our Streamer service");
            GrindActivity.this.bindService(i, GrindActivity.this, Context.BIND_AUTO_CREATE);
            prepareStream();
        }
    };

    /**
     * Останавливаем стрим по клику на паузу
     */
    private View.OnClickListener stopClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String TAG = "StopClick";
            try {
                binder.stopAudio();
            } catch (RemoteException e) {
                Log.e(TAG, "Cant stop");
            }
            initStream();
        }
    };
}
