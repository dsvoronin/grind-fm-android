package com.dsvoronin.grindfm.activity;

import android.app.Activity;
import android.content.*;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.*;
import com.dsvoronin.grindfm.GrindService;
import com.dsvoronin.grindfm.IGrindPlayer;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.view.MenuButton;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: dsvoronin
 * Date: 03.04.12
 * Time: 0:18
 */
public abstract class BaseActivity extends Activity implements MenuButton.Pickable, ServiceConnection {

    protected Map<Integer, MenuButton> menuButtons = new LinkedHashMap<Integer, MenuButton>();

    private GrindReceiver grindReceiver;

    private IGrindPlayer.Stub binder = null;

    private TextView headerRunningString;
    private ImageView playPause;
    private ProgressBar progressBar;
    private RelativeLayout radioControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

            MenuButton radioButton = (MenuButton) findViewById(R.id.menu_radio);
            radioButton.setOnPickListener(new MenuButton.OnPickListener() {
                @Override
                public void onPick() {
                    if (BaseActivity.this.getClass() != MainActivity.class) {
                        Intent intent = new Intent(getBaseContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                    }
                }
            });
            menuButtons.put(0, radioButton);

            MenuButton newsButton = (MenuButton) findViewById(R.id.menu_news);
            newsButton.setOnPickListener(new MenuButton.OnPickListener() {
                @Override
                public void onPick() {
                    Intent intent = new Intent(getBaseContext(), NewsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                }
            });
            menuButtons.put(1, newsButton);

            MenuButton newsGohaButton = (MenuButton) findViewById(R.id.menu_news_goha);
            newsGohaButton.setOnPickListener(new MenuButton.OnPickListener() {
                @Override
                public void onPick() {
                    Intent intent = new Intent(getBaseContext(), NewsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    intent.putExtra(getString(R.string.intent_news_feed), getString(R.string.rss_goha_main));
                    startActivity(intent);
                }
            });
            menuButtons.put(2, newsGohaButton);

            MenuButton videoButton = (MenuButton) findViewById(R.id.menu_video);
            videoButton.setOnPickListener(new MenuButton.OnPickListener() {
                @Override
                public void onPick() {
                    if (BaseActivity.this.getClass() != VideoActivity.class) {
                        Intent intent = new Intent(getBaseContext(), VideoActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                    }
                }
            });
            menuButtons.put(3, videoButton);

            MenuButton vkButton = (MenuButton) findViewById(R.id.menu_vk);
            vkButton.setOnPickListener(new MenuButton.OnPickListener() {
                @Override
                public void onPick() {
                    if (BaseActivity.this.getClass() != VKActivity.class) {
                        Intent intent = new Intent(getBaseContext(), VKActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                    }
                }
            });
            menuButtons.put(4, vkButton);

            headerRunningString = (TextView) findViewById(R.id.header_running_string);
            headerRunningString.setSelected(true);

            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            playPause = (ImageView) findViewById(R.id.play_pause);
            radioControl = (RelativeLayout) findViewById(R.id.radio_contol);
        }
    }

    @Override
    protected void onResume() {
        grindReceiver = new GrindReceiver();
        registerReceiver(grindReceiver, new IntentFilter(getString(R.string.service_intent)));

        Intent i = new Intent(BaseActivity.this, GrindService.class);
        BaseActivity.this.bindService(i, BaseActivity.this, Context.BIND_AUTO_CREATE);

        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(grindReceiver);
        super.onPause();
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
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        String TAG = "OnServiceConnected";
        Log.d(TAG, "START");

        binder = (IGrindPlayer.Stub) service;

        try {
            Log.d(TAG, "Is service playing audio? " + binder.playing());

            startService(new Intent(this, GrindService.class));
        } catch (RemoteException e) {
            Log.e(TAG, "onServiceConnected", e);
        }

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            try {
                if (binder.playing()) {
                    startStream();
                } else {
                    initStream();
                }
            } catch (RemoteException e) {
                Log.e("Activity", "play", e);
            }
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
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                String info = intent.getStringExtra(getString(R.string.service_intent_info));
                headerRunningString.setText(info);
                startStream();
            }
        }
    }

    private void initStream() {
        headerRunningString.setText(R.string.radio_loading);
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

    /**
     * Запускаем стрим по клику на плей
     */
    private View.OnClickListener playClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String TAG = "PlayClick";

            try {
                if (binder != null && binder.playing()) {
                    binder.stopAudio();
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Cant stop");
            }
            Log.d(TAG, "Bind to our Streamer service");
            try {
                if (binder != null) {
                    binder.startAudio();
                }
            } catch (RemoteException e) {
                Toast.makeText(BaseActivity.this, "Ошибка", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Cant play");
            }
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
