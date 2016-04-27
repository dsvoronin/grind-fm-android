package fm.grind.android.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Arrays;

import fm.grind.android.MainActivity;
import fm.grind.android.R;

public class GrindWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "GrindWidget";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate: " + Arrays.toString(appWidgetIds));
        for (int i = 0; i <= appWidgetIds.length - 1; i++) {
            RemoteViews remoteViews = updateWidgetListView(context, appWidgetIds[i]);

            Intent intent = new Intent(context, MainActivity.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            remoteViews.setPendingIntentTemplate(R.id.widget_list, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    private RemoteViews updateWidgetListView(Context context, int appWidgetId) {
        Log.d(TAG, "updateWidgetListView: " + appWidgetId);
        //which layout to show on widget
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

        //RemoteViews Service needed to provide adapter for ListView
        Intent svcIntent = new Intent(context, WidgetService.class);
        //passing app widget id to that RemoteViews Service
        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        //setting a unique Uri to the intent
        //don't know its purpose to me right now
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
        //setting adapter to listview of the widget
        remoteViews.setRemoteAdapter(R.id.widget_list, svcIntent);
        //setting an empty view in case of no data
        remoteViews.setEmptyView(R.id.widget_list, R.id.empty_view);
        return remoteViews;
    }
}
