package com.dsvoronin.grindfm;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class GrindWidget extends AppWidgetProvider {

    public static final String ACTION_NOTIFY = "grind-widget-notify";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

        remoteViews.setOnClickPendingIntent(R.id.widget_logo,
                PendingIntent.getActivity(
                        context
                        , 0,
                        new Intent(context, GrindActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT)
        );

        remoteViews.setOnClickPendingIntent(R.id.widget_play,
                PendingIntent.getService(
                        context,
                        0,
                        new Intent(context, GrindService.class),
                        PendingIntent.FLAG_UPDATE_CURRENT
                ));

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_NOTIFY)) {
            String msg = intent.getStringExtra("msg");

        }
        super.onReceive(context, intent);
    }
}
