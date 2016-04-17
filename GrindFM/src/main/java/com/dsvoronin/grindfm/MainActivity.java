package com.dsvoronin.grindfm;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.dsvoronin.grindfm.player.PlayerService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GrindFM.MainActivity";
    private String[] mPlanetTitles;
    private Menu menu;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isPlaying = intent.getBooleanExtra("isPlaying", false);
            Log.d(TAG, "Got: isplaying=" + isPlaying);

            if (menu != null) {
                MenuItem item = menu.findItem(R.id.action_player);
                if (item != null) {
                    item.setIcon(isPlaying ? R.drawable.av_pause : R.drawable.av_play);
                    item.setTitle(isPlaying ? R.string.action_player_pause : R.string.action_player_play);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPlanetTitles = getResources().getStringArray(R.array.navigation_array);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        if (savedInstanceState == null) {
            selectItem(0);
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_tracklist, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        switch (item.getItemId()) {
            case R.id.action_player:
                startService(new Intent(this, PlayerService.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void selectItem(int position) {
        Fragment fragment;

        switch (position) {
            case 0:
                fragment = new NewsFragment();
                break;
            case 1:
                fragment = new TrackListFragment();
                break;
            default:
                throw new IndexOutOfBoundsException("No such position: " + position);
        }
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // update selected item and title, then close the drawer
        setTitle(mPlanetTitles[position]);
    }

    @Override
    public void setTitle(CharSequence title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    public enum Action {
        PLAYER_STATUS_UPDATE
    }
}
