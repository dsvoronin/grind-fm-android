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
import com.dsvoronin.grindfm.util.StringUtil;

/**
 * User: dsvoronin
 * Date: 05.04.12
 * Time: 0:17
 */
public class GrindWidgetProvider extends AppWidgetProvider {

    private final String TAG = "GRIND-WIDGET";

    private Context mContext;

    private AppWidgetManager widgetManager;

    private ComponentName widgetProvider;

    private RemoteViews views;

    private ServiceHandler handler;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate");

        mContext = context;

        widgetManager = AppWidgetManager.getInstance(mContext);
        views = new RemoteViews(mContext.getPackageName(), R.layout.grind_widget);
        widgetProvider = new ComponentName(mContext, GrindWidgetProvider.class);

        setClickToLogo();
        setClickToPlay();
        setTextToDefault();
        widgetManager.updateAppWidget(widgetManager.getAppWidgetIds(widgetProvider), views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Got broadcast: " + intent.getAction());
        mContext = context;

        if (handler == null) {
            handler = new ServiceHandlerWidgetImpl();
        }

        if (widgetManager == null) {
            widgetManager = AppWidgetManager.getInstance(mContext);
        }

        if (views == null) {
            views = new RemoteViews(mContext.getPackageName(), R.layout.grind_widget);
        }

        if (widgetProvider == null) {
            widgetProvider = new ComponentName(mContext, GrindWidgetProvider.class);
        }

        if (intent.getAction().equals("service-intent")) {
            int command = intent.getIntExtra("service-command", -1);
            if (command != -1) {
                handler.sendEmptyMessage(command);
            } else {
                Log.d(TAG, "Incorrect command: -1");
            }

            String message = intent.getStringExtra("service-message");
            if (message != null) {
                Log.d(TAG, "Got service message");
                updateText(StringUtil.widgetString(message));
                widgetManager.updateAppWidget(widgetManager.getAppWidgetIds(widgetProvider), views);
            }
        } else {
            super.onReceive(context, intent);
        }
    }

    private class ServiceHandlerWidgetImpl extends ServiceHandler {

        @Override
        protected void handleProgress() {
            Log.d(TAG, "Got process command");

            setProgress();
            widgetManager.updateAppWidget(widgetManager.getAppWidgetIds(widgetProvider), views);
        }

        @Override
        protected void handleStop() {
            Log.d(TAG, "Got stop command");

            mContext.stopService(new Intent(mContext, GrindService.class));
            setClickToPlay();
            setTextToDefault();
            widgetManager.updateAppWidget(widgetManager.getAppWidgetIds(widgetProvider), views);
        }

        @Override
        protected void handleStart() {
            Log.d(TAG, "Got start command");

            setButtonPause();
            widgetManager.updateAppWidget(widgetManager.getAppWidgetIds(widgetProvider), views);
        }
    }

    private void setClickToLogo() {
        Intent intent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_to_activity, pendingIntent);
    }

    private void setClickToPlay() {
        Intent intent = new Intent(mContext, GrindService.class);
        PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);
        views.setImageViewResource(R.id.widget_button, android.R.drawable.ic_media_play);
    }

    private void setButtonPause() {
        Log.d(TAG, "GOTCHA");

//        Intent intent = new Intent("service-intent");
//        intent.putExtra("service-command", ServiceHandler.COMMAND_STOP);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
//        views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);
        views.setImageViewResource(R.id.widget_button, android.R.drawable.ic_media_pause);
//        views.setViewVisibility(R.id.widget_button, View.VISIBLE);
//        views.setViewVisibility(R.id.widget_progress_bar, View.GONE);
    }

    private void setTextToDefault() {
        views.setTextViewText(R.id.widget_text, mContext.getString(R.string.radio_loading));
    }

    protected void updateText(String info) {
        views.setTextViewText(R.id.widget_text, info);
    }

    private void setProgress() {
        views.setViewVisibility(R.id.widget_button, View.GONE);
        views.setViewVisibility(R.id.widget_progress_bar, View.VISIBLE);
    }
}
