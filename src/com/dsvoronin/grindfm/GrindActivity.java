package com.dsvoronin.grindfm;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.*;
import com.dsvoronin.R;
import com.dsvoronin.grindfm.adapter.NewsAdapter;
import com.dsvoronin.grindfm.adapter.VideoAdapter;
import com.dsvoronin.grindfm.task.RssParseTask;
import com.dsvoronin.grindfm.task.VideoTask;
import com.dsvoronin.grindfm.util.ImageManager;
import com.dsvoronin.grindfm.view.GesturableViewFlipper;

import java.io.IOException;

public class GrindActivity extends Activity implements GesturableViewFlipper.OnSwitchListener, AdapterView.OnItemClickListener {

    private static final int MENU_ID_RADIO = 0;
    private static final int MENU_ID_NEWS = 1;
    private static final int MENU_ID_VIDEO = 2;

    private static final int MEDIA_NOT_READY = 0;
    private static final int MEDIA_READY = 1;
    private static final int MEDIA_PLAYING = 2;

    private static final String TAG = GrindActivity.class.getSimpleName();

    private int state = MEDIA_NOT_READY;

    private boolean newsFirstStart = true;
    private boolean videoFirstStart = true;

    private Button button;
    private ProgressBar progressBar;
    private GesturableViewFlipper flipper;

    private ListView newsList;
    private ListView videoList;

    private TextView menuTextRadio;
    private TextView menuTextNews;
    private TextView menuTextVideo;

    private NewsAdapter mNewsAdapter;
    private VideoAdapter mVideoAdapter;

    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.grind);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        ImageManager imageManager = ImageManager.getInstance();
        imageManager.init(this,
                Environment.getExternalStorageDirectory().getPath() + "/Android/data/" + getPackageName() + "/cache/");
        imageManager.setFileCache(true);

        menuTextNews = (TextView) findViewById(R.id.menuNewsText);
        menuTextRadio = (TextView) findViewById(R.id.menuRadioText);
        menuTextVideo = (TextView) findViewById(R.id.menuVideoText);

        mNewsAdapter = new NewsAdapter(this);
        newsList = (ListView) findViewById(R.id.newsList);
        newsList.setAdapter(mNewsAdapter);
        newsList.setOnItemClickListener(this);

        mVideoAdapter = new VideoAdapter(this);
        videoList = (ListView) findViewById(R.id.videoList);
        videoList.setAdapter(mVideoAdapter);
        videoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mVideoAdapter.getItem(i).getUrl())));
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        button = (Button) findViewById(R.id.button);

        flipper = (GesturableViewFlipper) findViewById(R.id.flipper);
        flipper.setOnSwitchListener(this);

        final MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(this, Uri.parse(getString(R.string.radio_stream_url_ogg)));
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
                        Notification notification = new Notification(R.drawable.paw_logo, "You are listening to Grind.FM", System.currentTimeMillis());
                        notificationManager.notify(1, notification);
                        break;
                    case MEDIA_PLAYING:
                        mediaPlayer.pause();
                        state = MEDIA_READY;
                        button.setBackgroundResource(android.R.drawable.ic_media_play);
                        notificationManager.cancel(1);
                        break;
                    default:
                        break;
                }
            }
        });
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
                menuTextRadio.setVisibility(View.VISIBLE);
                menuTextNews.setVisibility(View.GONE);
                menuTextVideo.setVisibility(View.GONE);
                break;
            case MENU_ID_NEWS:
                menuTextRadio.setVisibility(View.GONE);
                menuTextNews.setVisibility(View.VISIBLE);
                menuTextVideo.setVisibility(View.GONE);
                if (newsFirstStart) {
                    RssParseTask task = new RssParseTask(this, mNewsAdapter);
                    task.execute(getString(R.string.news_url));
                    newsFirstStart = false;
                }
                break;
            case MENU_ID_VIDEO:
                menuTextRadio.setVisibility(View.GONE);
                menuTextNews.setVisibility(View.GONE);
                menuTextVideo.setVisibility(View.VISIBLE);
                if (videoFirstStart) {
                    VideoTask task = new VideoTask(this, mVideoAdapter);
                    task.execute(getString(R.string.youtube_playlist_postrelushka));
                    videoFirstStart = false;
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        NewsDialog dialog = new NewsDialog(this, mNewsAdapter.getItem(i));
        dialog.show();
    }
}
