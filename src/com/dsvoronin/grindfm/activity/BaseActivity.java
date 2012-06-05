package com.dsvoronin.grindfm.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.dsvoronin.grindfm.GrindService;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.ServiceHandler;
import com.dsvoronin.grindfm.util.StringUtil;

/**
 * User: dsvoronin
 * Date: 03.04.12
 * Time: 0:18
 */
public abstract class BaseActivity extends Activity {

    private final String TAG = "GRIND-ACTIVITY";

    private GrindReceiver grindReceiver;

    private ServiceHandler handler;

    private TextView headerRunningString;
    private ImageView playPause;
    private ProgressBar progressBar;
    private RelativeLayout radioControl;

    private static boolean messageFlag = false;
    private Handler playerHandler;
    private Runnable handleMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            headerRunningString = (TextView) findViewById(R.id.header_running_string);
            headerRunningString.setSelected(true);

            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            playPause = (ImageView) findViewById(R.id.play_pause);
            radioControl = (RelativeLayout) findViewById(R.id.radio_contol);
        }
    }

    @Override
    protected void onResume() {
        if (grindReceiver == null) {
            grindReceiver = new GrindReceiver();
            registerReceiver(grindReceiver, new IntentFilter("service-intent"));
        }

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

            Log.d(TAG, "Sending player command");
            Intent intent = new Intent("player-intent");
            intent.putExtra("player-command", GrindService.COMMAND_GET_STATUS);
            this.sendBroadcast(intent);

            messageFlag = false;

            playerHandler = new Handler();
            handleMessage = new Runnable() {
                @Override
                public void run() {
                    if (!messageFlag) {
                        initStream();
                    }
                }
            };
            playerHandler.postDelayed(handleMessage, 1500);

            int buttonId = getIntent().getIntExtra("button-id", -1);

            RadioGroup footerGroup = (RadioGroup) findViewById(R.id.footer_radio_group);
            if (buttonId == -1) {
                footerGroup.check(R.id.menu_radio);
            } else {
                footerGroup.check(buttonId);
            }
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
        if (playerHandler != null) {
            messageFlag = false;
            playerHandler.removeCallbacks(handleMessage);
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(grindReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (this instanceof MainActivity) {
                return super.onKeyDown(keyCode, event);
            }

            if (!(this instanceof NewsDetailsActivity)) {
                Intent a = new Intent(this, MainActivity.class);
                a.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(a);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void menuClick(Class<?> destinationActivityClass, int buttonId) {
        Intent intent = new Intent(this, destinationActivityClass);
        intent.putExtra("button-id", buttonId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    private void menuClick(Class<?> destinationActivityClass, int buttonId, String extraNewsFeed) {
        Intent intent = new Intent(this, destinationActivityClass);
        intent.putExtra("button-id", buttonId);
        intent.putExtra(NewsActivity.INTENT_EXTRA, extraNewsFeed);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    public void radioClick(View view) {
        menuClick(MainActivity.class, R.id.menu_radio);
    }

    public void newsClick(View view) {
        menuClick(NewsActivity.class, R.id.menu_news, getString(R.string.rss_goha_grindfm));
    }

    public void gohaClick(View view) {
        menuClick(NewsActivity.class, R.id.menu_news_goha, getString(R.string.rss_goha_main));
    }

    public void youtubeClick(View view) {
        menuClick(VideoActivity.class, R.id.menu_video);
    }

    public void vkClick(View view) {
        menuClick(VKActivity.class, R.id.menu_vk);
    }

    private class GrindReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

                if (handler == null) {
                    handler = new ServiceHandlerWidgetImpl();
                }

                int command = intent.getIntExtra("service-command", -1);
                if (command != -1) {
                    handler.sendEmptyMessage(command);
                    messageFlag = true;
                } else {
                    Log.d(TAG, "Incorrect command: -1");
                }

                String info = intent.getStringExtra("service-message");
                if (info != null) {
                    Log.d(TAG, "Got service message");
                    headerRunningString.setText(StringUtil.widgetString(info));
                }
            }
        }
    }

    private class ServiceHandlerWidgetImpl extends ServiceHandler {

        @Override
        protected void handleProgress() {
            Log.d(TAG, "Got process command");

            prepareStream();
        }

        @Override
        protected void handleStop() {
            Log.d(TAG, "Got stop command");

            initStream();
        }

        @Override
        protected void handleStart() {
            Log.d(TAG, "Got start command");

            startStream();
        }
    }

    private void initStream() {
        headerRunningString.setText(R.string.radio_loading);
        playPause.setImageResource(R.drawable.play);
        radioControl.setOnClickListener(playClickListener);
        playPause.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        playPause.setAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
    }

    private void prepareStream() {
        radioControl.setOnClickListener(null);
        playPause.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        progressBar.setAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
    }

    private void startStream() {
        radioControl.setOnClickListener(stopClickListener);
        playPause.setImageResource((R.drawable.pause));
        playPause.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        playPause.setAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
    }

    /**
     * Запускаем стрим по клику на плей
     */
    private View.OnClickListener playClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "Start Click");
            startService(new Intent(getBaseContext(), GrindService.class));
        }
    };

    /**
     * Останавливаем стрим по клику на паузу
     */
    private View.OnClickListener stopClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "Stop Click");
            stopService(new Intent(getBaseContext(), GrindService.class));
        }
    };
}
