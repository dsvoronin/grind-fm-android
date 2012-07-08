package com.dsvoronin.grindfm.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import com.dsvoronin.grindfm.PlayerService;
import com.dsvoronin.grindfm.R;

/**
 * User: dsvoronin
 * Date: 03.04.12
 * Time: 0:18
 */
public abstract class BaseActivity extends Activity {

    private final String TAG = "Grind.Activity";

    private GrindReceiver grindReceiver;

    private ImageView playPause;
    private ProgressBar progressBar;
    private RelativeLayout radioControl;

    private PlayerHandler playerHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            playPause = (ImageView) findViewById(R.id.play_pause);
            radioControl = (RelativeLayout) findViewById(R.id.radio_contol);
        }
    }

    @Override
    protected void onResume() {
        if (grindReceiver == null) {
            grindReceiver = new GrindReceiver();
        }
        registerReceiver(grindReceiver, new IntentFilter(PlayerService.ACTION_DISPLAY));

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

            Intent intent = new Intent(getBaseContext(), PlayerService.class);
            intent.setAction(PlayerService.ACTION_REQUEST);
            startService(intent);

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
            Log.d(TAG, "Got broadcast " + intent.getAction());
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (playerHandler == null) {
                    playerHandler = new PlayerHandler();
                }
                if (intent.getAction().equals(PlayerService.ACTION_DISPLAY)) {
                    int what = intent.getIntExtra(PlayerService.ACTION_DISPLAY, -1);
                    if (what != -1) {
                        playerHandler.sendEmptyMessage(what);
                    } else {
                        Log.d(TAG, "Incorrect command");
                    }
                }
            }
        }
    }


    private class PlayerHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 3:
                    prepareStream();
                    break;
                case 2:
                    initStream();
                    break;
                case 1:
                    startStream();
                    break;
                default:
                    break;
            }
        }
    }

    private void initStream() {
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
        radioControl.setOnClickListener(playClickListener);
        playPause.setImageResource((R.drawable.pause));
        playPause.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        playPause.setAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
    }

    private View.OnClickListener playClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d(TAG, "Start Click");
            Intent intent = new Intent(getBaseContext(), PlayerService.class);
            intent.setAction(PlayerService.ACTION_PLAY_PAUSE);
            startService(intent);
        }
    };
}
