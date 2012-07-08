package com.dsvoronin.grindfm.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import com.dsvoronin.grindfm.PlayerService;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.activity.MainActivity;

/**
 * User: dsvoronin
 * Date: 05.04.12
 * Time: 0:17
 */
public class GrindWidgetProvider extends AppWidgetProvider {

    private final String TAG = "Grind.Widget";

    private Context context;

    private AppWidgetManager widgetManager;

    private ComponentName widgetProvider;

    private RemoteViews views;

    private PlayerHandler handler;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate");

        this.context = context;

        widgetManager = AppWidgetManager.getInstance(this.context);
        views = new RemoteViews(this.context.getPackageName(), R.layout.grind_widget);
        widgetProvider = new ComponentName(this.context, GrindWidgetProvider.class);

        setClickToLogo();
        initStream();

        widgetManager.updateAppWidget(widgetManager.getAppWidgetIds(widgetProvider), views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Got broadcast: " + intent.getAction());
        this.context = context;

        if (widgetManager == null) {
            widgetManager = AppWidgetManager.getInstance(this.context);
        }

        if (views == null) {
            views = new RemoteViews(this.context.getPackageName(), R.layout.grind_widget);
        }

        if (widgetProvider == null) {
            widgetProvider = new ComponentName(this.context, GrindWidgetProvider.class);
        }

        if (handler == null) {
            handler = new PlayerHandler();
        }

        if (intent.getAction().equals(PlayerService.ACTION_DISPLAY)) {
            int extra = intent.getIntExtra(PlayerService.ACTION_DISPLAY, -1);
            if (extra != -1) {
                handler.sendEmptyMessage(extra);
            } else {
                Log.d(TAG, "Incorrect command");
            }
        } else {
            super.onReceive(this.context, intent);
        }
    }

    private void initStream() {
        views.setViewVisibility(R.id.widget_button, View.VISIBLE);
        views.setViewVisibility(R.id.widget_progress_bar, View.GONE);
        views.setOnClickPendingIntent(R.id.widget_button, buildPlayPauseIntent());
        views.setImageViewResource(R.id.widget_button, android.R.drawable.ic_media_play);
    }

    private void prepareStream() {
        views.setViewVisibility(R.id.widget_button, View.GONE);
        views.setViewVisibility(R.id.widget_progress_bar, View.VISIBLE);
    }

    private void startStream() {
        views.setViewVisibility(R.id.widget_button, View.VISIBLE);
        views.setViewVisibility(R.id.widget_progress_bar, View.GONE);
        views.setOnClickPendingIntent(R.id.widget_button, buildPlayPauseIntent());
        views.setImageViewResource(R.id.widget_button, android.R.drawable.ic_media_pause);
    }

    private void setClickToLogo() {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_logo, pendingIntent);
    }

    private PendingIntent buildPlayPauseIntent() {
        Intent intent = new Intent(context, PlayerService.class);
        intent.setAction(PlayerService.ACTION_PLAY_PAUSE);
        return PendingIntent.getService(context, 0, intent, 0);
    }

    private class PlayerHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PlayerService.DISPLAY_PROGRESS:
                    prepareStream();
                    break;
                case PlayerService.DISPLAY_PAUSED:
                    initStream();
                    break;
                case PlayerService.DISPLAY_PLAYING:
                    startStream();
                    break;
                default:
                    break;
            }
            widgetManager.updateAppWidget(widgetManager.getAppWidgetIds(widgetProvider), views);
        }
    }
}
