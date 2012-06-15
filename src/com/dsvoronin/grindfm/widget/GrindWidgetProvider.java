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
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.activity.MainActivity;

/**
 * User: dsvoronin
 * Date: 05.04.12
 * Time: 0:17
 */
public class GrindWidgetProvider extends AppWidgetProvider {

    private final String TAG = "Grind.Widget";

    private Context mContext;

    private AppWidgetManager widgetManager;

    private ComponentName widgetProvider;

    private RemoteViews views;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate");

        mContext = context;

        widgetManager = AppWidgetManager.getInstance(mContext);
        views = new RemoteViews(mContext.getPackageName(), R.layout.grind_widget);
        widgetProvider = new ComponentName(mContext, GrindWidgetProvider.class);

        setClickToLogo();
        setTextToDefault();
        widgetManager.updateAppWidget(widgetManager.getAppWidgetIds(widgetProvider), views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {


        Log.d(TAG, "Got broadcast: " + intent.getAction());
        mContext = context;

        if (widgetManager == null) {
            widgetManager = AppWidgetManager.getInstance(mContext);
        }

        if (views == null) {
            views = new RemoteViews(mContext.getPackageName(), R.layout.grind_widget);
        }

        if (widgetProvider == null) {
            widgetProvider = new ComponentName(mContext, GrindWidgetProvider.class);
        }

        setClickToLogo();

    }

    private void setClickToLogo() {
        Intent intent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_to_activity, pendingIntent);
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
