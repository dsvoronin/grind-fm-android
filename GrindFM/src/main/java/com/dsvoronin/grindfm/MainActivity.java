package com.dsvoronin.grindfm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.dsvoronin.grindfm.player.NewPlayerService;
import com.dsvoronin.grindfm.player.PlayerService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GrindFM.MainActivity";

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isPlaying = intent.getBooleanExtra("isPlaying", false);
            Log.d(TAG, "Got: isplaying=" + isPlaying);

//            if (menu != null) {
//                MenuItem item = menu.findItem(R.id.action_player);
//                if (item != null) {
//                    item.setIcon(isPlaying ? R.drawable.av_pause : R.drawable.av_play);
//                    item.setTitle(isPlaying ? R.string.action_player_pause : R.string.action_player_play);
//                }
//            }
        }
    };

    private Button playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new NewsFragment()).commit();
        }

        playButton = (Button) findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(MainActivity.this, NewPlayerService.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, new IntentFilter(Action.PLAYER_STATUS_UPDATE.name()));

        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(PlayerService.Action.REQUEST_STATUS.name()));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
        super.onPause();
    }

    public enum Action {
        PLAYER_STATUS_UPDATE
    }
}
