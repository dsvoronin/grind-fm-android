package com.dsvoronin.grindfm.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import com.dsvoronin.grindfm.GrindService;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.ServiceHandler;
import com.dsvoronin.grindfm.activity.MainActivity;

/**
 * User: dsvoronin
 * Date: 05.04.12
 * Time: 0:17
 */
public class GrindWidgetProvider extends AppWidgetProvider {

    private final String TAG = "GRIND-WIDGET";

    private Context mContext;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate");

        mContext = context;

        setClickToLogo();
        setClickToPlay();
        setTextToDefault();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Got broadcast: " + intent.getAction());
        mContext = context;
        if (intent.getAction().equals(context.getString(R.string.service_intent))) {
            int message = intent.getIntExtra(context.getString(R.string.service_intent_message), -1);
            if (message != -1) {
                Log.d(TAG, "Correct message. Go handle it!");
                handler.sendEmptyMessage(message);
            } else {
                Log.d(TAG, "Incorrect message: -1");
            }
        }
        super.onReceive(context, intent);
    }

    private ServiceHandler handler = new ServiceHandler(mContext) {

        @Override
        protected void handleProgress() {
            setProgress();
        }

        @Override
        protected void handleStop() {
            setClickToPlay();
            setTextToDefault();
        }
    };

    private void setClickToLogo() {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(mContext);
        ComponentName widgetProvider = new ComponentName(mContext, GrindWidgetProvider.class);
        Intent intent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.grind_widget);
        views.setOnClickPendingIntent(R.id.widget_to_activity, pendingIntent);
        widgetManager.updateAppWidget(widgetManager.getAppWidgetIds(widgetProvider), views);
    }

    private void setClickToPlay() {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(mContext);
        ComponentName widgetProvider = new ComponentName(mContext, GrindWidgetProvider.class);
        Intent intent = new Intent(mContext, GrindService.class);
        PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, intent, 0);
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.grind_widget);
        views.setOnClickPendingIntent(R.id.widget_play, pendingIntent);
        views.setImageViewResource(R.id.widget_play, android.R.drawable.ic_media_play);
        widgetManager.updateAppWidget(widgetManager.getAppWidgetIds(widgetProvider), views);
    }

    private void setTextToDefault() {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(mContext);
        ComponentName widgetProvider = new ComponentName(mContext, GrindWidgetProvider.class);
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.grind_widget);
        views.setTextViewText(R.id.widget_text, mContext.getString(R.string.radio_loading));
        widgetManager.updateAppWidget(widgetManager.getAppWidgetIds(widgetProvider), views);
    }

    private void setProgress() {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(mContext);
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.grind_widget);
        views.setViewVisibility(R.id.widget_play, View.GONE);
        views.setViewVisibility(R.id.widget_progress_bar, View.VISIBLE);
        ComponentName widgetProvider = new ComponentName(mContext, GrindWidgetProvider.class);
        widgetManager.updateAppWidget(widgetManager.getAppWidgetIds(widgetProvider), views);
    }
}
