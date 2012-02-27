package com.dsvoronin.grindfm;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import com.dsvoronin.grindfm.adapter.NewsAdapter;
import com.dsvoronin.grindfm.adapter.VideoAdapter;
import com.dsvoronin.grindfm.task.RssParseTask;
import com.dsvoronin.grindfm.task.VideoTask;
import com.dsvoronin.grindfm.util.YouTubeUtil;
import com.dsvoronin.grindfm.view.GesturableViewFlipper;

public class GrindActivity extends Activity implements GesturableViewFlipper.OnSwitchListener {

    private static final int MENU_ID_RADIO = 0;
    private static final int MENU_ID_NEWS = 1;
    private static final int MENU_ID_VIDEO = 2;
    private static final int MENU_ID_VKONTAKTE = 3;

    private static final int MEDIA_NOT_READY = 0;
    private static final int MEDIA_READY = 1;
    private static final int MEDIA_PLAYING = 2;

    private int state = MEDIA_READY;

    private boolean newsFirstStart = true;
    private boolean videoFirstStart = true;

    private ImageView playPause;
    private ProgressBar progressBar;
    private GesturableViewFlipper flipper;
    private ListView newsList;
    private ListView videoList;
    private TextView newsProgress;
    private TextView videoProgress;
    private TextView headerRunningString;
    private WebView vkontakteWebView;

    private NewsAdapter mNewsAdapter;
    private VideoAdapter mVideoAdapter;

    private GrindReceiver grindReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.grind);
        initViews();

        vkontakteWebView.setWebViewClient(new VkontakteWebViewClient());

        //включает бегущую строку
        headerRunningString.setSelected(true);

        mNewsAdapter = new NewsAdapter(this);
        newsList.setAdapter(mNewsAdapter);
        newsList.setOnItemClickListener(onNewsItemClickListener);

        mVideoAdapter = new VideoAdapter(this);
        videoList.setAdapter(mVideoAdapter);
        videoList.setOnItemClickListener(onVideoItemClickListener);

        flipper.setOnSwitchListener(this);
    }

    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter(getString(R.string.service_intent));
        grindReceiver = new GrindReceiver();
        registerReceiver(grindReceiver, filter);
        if (isMyServiceRunning()) {
            setState(MEDIA_PLAYING);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(grindReceiver);
        super.onPause();
    }

    @SuppressWarnings("unused")
    public void onRadioClick(View view) {
        setState(state);
    }

    @SuppressWarnings("unused")
    public void toRadio(View view) {
        onSwitch(MENU_ID_RADIO);
    }

    @SuppressWarnings("unused")
    public void toNews(View view) {
        onSwitch(MENU_ID_NEWS);
    }

    @SuppressWarnings("unused")
    public void toVideo(View view) {
        onSwitch(MENU_ID_VIDEO);
    }

    @SuppressWarnings("unused")
    public void toVkontakte(View view) {
        onSwitch(MENU_ID_VKONTAKTE);
    }

    @Override
    public void onSwitch(int childId) {
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

        switch (flipper.getDisplayedChild()) {
            case MENU_ID_RADIO:
                break;
            case MENU_ID_NEWS:
                if (newsFirstStart) {
                    RssParseTask task = new RssParseTask(this, mNewsAdapter);
                    task.setProgress(newsProgress);
                    task.execute(getString(R.string.news_url));
                    newsFirstStart = false;
                }
                break;
            case MENU_ID_VIDEO:
                if (videoFirstStart) {
                    VideoTask task = new VideoTask(this, mVideoAdapter);
                    task.setProgress(videoProgress);
                    task.execute(getString(R.string.youtube_playlist_postrelushka));
                    videoFirstStart = false;
                }
                break;
            case MENU_ID_VKONTAKTE:
                vkontakteWebView.loadUrl(getString(R.string.vkontakte_url));
                vkontakteWebView.requestFocus(View.FOCUS_DOWN);
                break;

        }
    }

    private void initViews() {
        vkontakteWebView = (WebView) findViewById(R.id.vkontakteWebView);
        headerRunningString = (TextView) findViewById(R.id.header_running_string);
        newsList = (ListView) findViewById(R.id.newsList);
        videoList = (ListView) findViewById(R.id.videoList);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        playPause = (ImageView) findViewById(R.id.play_pause);
        flipper = (GesturableViewFlipper) findViewById(R.id.flipper);
        newsProgress = (TextView) findViewById(R.id.news_progress);
        videoProgress = (TextView) findViewById(R.id.video_progress);
    }

    public void setState(int state) {
        switch (state) {
            case MEDIA_NOT_READY:
                break;
            case MEDIA_READY:
                Intent intent = new Intent(this, GrindService.class);
                startService(intent);
                playPause.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                state = MEDIA_NOT_READY;
                break;
            case MEDIA_PLAYING:
                stopService(new Intent(this, GrindService.class));
                playPause.setImageResource(android.R.drawable.ic_media_play);
                state = MEDIA_READY;
                break;
        }
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

    private class GrindReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String info = intent.getStringExtra(getString(R.string.service_intent_info));
            headerRunningString.setText(info);

            playPause.setImageResource(android.R.drawable.ic_media_pause);

            playPause.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

            state = MEDIA_PLAYING;
        }
    }

    private class VkontakteWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    private AdapterView.OnItemClickListener onVideoItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(YouTubeUtil.YOUTUBE_VIDEO + mVideoAdapter.getItem(i).getUrl())));
        }
    };

    private AdapterView.OnItemClickListener onNewsItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            NewsDialog dialog = new NewsDialog(GrindActivity.this, mNewsAdapter.getItem(i));
            dialog.show();
        }
    };
}
