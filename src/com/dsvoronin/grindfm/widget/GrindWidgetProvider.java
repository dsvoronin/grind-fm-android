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
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.activity.MainActivity;

/**
 * User: dsvoronin
 * Date: 05.04.12
 * Time: 0:17
 */
public class GrindWidgetProvider extends AppWidgetProvider {

    private final String TAG = "GRIND-WIDGET";

    private Context mContext;

    private RemoteViews mRemoteViews;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate");

        mContext = context;
        mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.grind_widget);

        setClickToLogo();
        setClickToPlay();

        appWidgetManager.updateAppWidget(appWidgetIds, mRemoteViews);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Got broadcast: " + intent.getAction());
        mContext = context;
        if (intent.getAction().equals(context.getString(R.string.service_intent))) {
            int message = intent.getIntExtra(context.getString(R.string.service_intent_message), -1);
            if (message != -1) {
                Log.d(TAG, "Send status update");
                handler.sendEmptyMessage(message);
            } else {
                Log.d(TAG, "No update to send: -1");
            }
        }
        super.onReceive(context, intent);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == mContext.getResources().getInteger(R.integer.service_intent_message_progress)) {
                Log.d(TAG, "Got Progress message");
                setProgress();
            }
        }
    };

    private void setClickToLogo() {
        Intent intent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        mRemoteViews.setOnClickPendingIntent(R.id.widget_to_activity, pendingIntent);
    }

    private void setClickToPlay() {
        Intent intent = new Intent(mContext.getString(R.string.widget_intent));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
        mRemoteViews.setOnClickPendingIntent(R.id.widget_play, pendingIntent);
    }

    private void setProgress() {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(mContext);
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.grind_widget);
        views.setViewVisibility(R.id.widget_play, View.GONE);
        views.setViewVisibility(R.id.progress_bar, View.VISIBLE);
        ComponentName widgetProvider = new ComponentName(mContext, GrindWidgetProvider.class);
        widgetManager.updateAppWidget(widgetManager.getAppWidgetIds(widgetProvider), views);
    }
}
