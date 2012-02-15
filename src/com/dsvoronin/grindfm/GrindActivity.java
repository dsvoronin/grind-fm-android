package com.dsvoronin.grindfm;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import com.dsvoronin.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GrindActivity extends Activity {

    private static final int MEDIA_NOT_READY = 0;
    private static final int MEDIA_READY = 1;
    private static final int MEDIA_PLAYING = 2;

    private static final String TAG = GrindActivity.class.getSimpleName();

    private int state = MEDIA_NOT_READY;
    private Button button;
    private ProgressBar progressBar;
    private HorizontalListView listMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.grind);

        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        button = (Button) findViewById(R.id.button);
        listMenu = (HorizontalListView) findViewById(R.id.main_menu);
        listMenu.setAdapter(mAdapter);

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

    private class MenuItem {
        private int resId;
        private String title;

        private MenuItem(int resId, String title) {
            this.resId = resId;
            this.title = title;
        }

        public int getResId() {
            return resId;
        }

        public String getTitle() {
            return title;
        }
    }

    private List<MenuItem> menuItemList = new ArrayList<MenuItem>() {
        {
            add(new MenuItem(R.drawable.microphone, "Радио"));
            add(new MenuItem(R.drawable.calendar, "Расписание"));
            add(new MenuItem(R.drawable.news, "Новости"));
            add(new MenuItem(R.drawable.headphones, "Заказ песен"));
            add(new MenuItem(R.drawable.youtube_play_icon, "Видео"));
        }
    };

    private BaseAdapter mAdapter = new BaseAdapter() {

        @Override
        public int getCount() {
            return menuItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View retval = getLayoutInflater().inflate(R.layout.listmenu_item, null);
            TextView title = (TextView) retval.findViewById(R.id.title);
            title.setText(menuItemList.get(position).getTitle());
            ImageView imageView = (ImageView) retval.findViewById(R.id.image);
            imageView.setImageResource(menuItemList.get(position).getResId());
            return retval;
        }

    };
}
