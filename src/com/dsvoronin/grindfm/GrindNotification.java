package com.dsvoronin.grindfm;

import android.app.Notification;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

/**
 * User: dsvoronin
 * Date: 05.06.12
 * Time: 15:13
 */
public class GrindNotification extends Notification {

    private Context context;
    private String meta;

    public GrindNotification(Context context, String meta) {
        this.context = context;
        this.meta = meta;
    }

    public Notification buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentIntent(contentIntent);
        builder.setSmallIcon(R.drawable.cat_status_bar);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.cat_status_bar_large));
        builder.setTicker(context.getString(R.string.app_name));
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(false);
        builder.setContentTitle(context.getString(R.string.app_name));
        builder.setContentText(meta);
        builder.setOngoing(true);
        return builder.getNotification();
    }
}
