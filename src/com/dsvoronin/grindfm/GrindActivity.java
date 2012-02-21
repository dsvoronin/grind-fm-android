package com.dsvoronin.grindfm;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import com.dsvoronin.R;

import java.io.IOException;

public class GrindActivity extends Activity {
    private static final int MEDIA_NOT_READY = 0;
    private static final int MEDIA_READY = 1;
    private static final int MEDIA_PLAYING = 2;

    private static final String TAG = GrindActivity.class.getSimpleName();

    private int state = MEDIA_NOT_READY;

    private boolean newsFirstStart = true;

    private Button button;
    private ProgressBar progressBar;
    private GesturableViewFlipper flipper;
    private ListView newsList;

    private NewsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.grind);

        mAdapter = new NewsAdapter(this);
        newsList = (ListView) findViewById(R.id.newsList);
        newsList.setAdapter(mAdapter);

        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        button = (Button) findViewById(R.id.button);
        flipper = (GesturableViewFlipper) findViewById(R.id.flipper);

        flipper.setOnSwitchListener(new GesturableViewFlipper.OnSwitchListener() {
            @Override
            public void onSwitch(int child) {
                flip(child);
            }
        });

        final MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(this, Uri.parse("http://radio.goha.ru:8000/grindfm.ogg"));
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    button.setBackgroundResource(android.R.drawable.ic_media_play);
                    state = MEDIA_READY;
                    progressBar.setVisibility(View.GONE);
                    button.setVisibility(View.VISIBLE);
                }
            });
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e(TAG, "Error while opening stream", e);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (state) {
                    case MEDIA_READY:
                        mediaPlayer.start();
                        state = MEDIA_PLAYING;
                        button.setBackgroundResource(android.R.drawable.ic_media_pause);
                        break;
                    case MEDIA_PLAYING:
                        mediaPlayer.pause();
                        state = MEDIA_READY;
                        button.setBackgroundResource(android.R.drawable.ic_media_play);
                    default:
                        break;
                }
            }
        });
    }

    private void flip(int childId) {
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

        if (flipper.getDisplayedChild() == 1) {
            newsList.setSelectionAfterHeaderView();
            if (newsFirstStart) {
                RssParseTask task = new RssParseTask(this, mAdapter);
                task.execute(getString(R.string.news_url));
                newsFirstStart = false;
            }
        }

        Log.i("!!!!!", "CURRENT VIEW = " + flipper.getDisplayedChild());
    }

    @SuppressWarnings("unused")
    public void toRadio(View view) {
        flip(0);
    }

    @SuppressWarnings("unused")
    public void toNews(View view) {
        flip(1);
    }
}
