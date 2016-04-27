package fm.grind.android.widget;

import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import fm.grind.android.R;

import static android.provider.BaseColumns._ID;
import static fm.grind.android.sync.GrindProvider.Contract.Entry.COLUMN_NAME_FORMATTED_DATE;
import static fm.grind.android.sync.GrindProvider.Contract.Entry.COLUMN_NAME_IMAGE_URL;
import static fm.grind.android.sync.GrindProvider.Contract.Entry.COLUMN_NAME_LINK;
import static fm.grind.android.sync.GrindProvider.Contract.Entry.COLUMN_NAME_PUB_DATE;
import static fm.grind.android.sync.GrindProvider.Contract.Entry.COLUMN_NAME_TITLE;
import static fm.grind.android.sync.GrindProvider.Contract.Entry.CONTENT_URI;

public class WidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = "GrindWidget";

    /**
     * Projection for querying the content provider.
     */
    private static final String[] PROJECTION = new String[]{
            _ID,
            COLUMN_NAME_TITLE,
            COLUMN_NAME_LINK,
            COLUMN_NAME_IMAGE_URL,
            COLUMN_NAME_FORMATTED_DATE
    };

    // Column indexes. The index of a column in the Cursor is the same as its relative position in
    // the projection. 0 = _ID
    private static final int COLUMN_ID = 0;
    private static final int COLUMN_TITLE = 1;
    private static final int COLUMN_LINK = 2;
    private static final int COLUMN_IMAGE_URL = 3;
    private static final int COLUMN_FORMATTED_DATE = 4;

    private Context context;
    private ContentResolver contentResolver;
    private Cursor cursor;
    private int appWidgetId;

    public WidgetViewsFactory(Context context, Intent intent) {
        this.context = context;
        this.contentResolver = context.getContentResolver();
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onDataSetChanged() {
        Log.d(TAG, "onDataSetChanged");
        cursor = contentResolver.query(CONTENT_URI, PROJECTION, null, null, COLUMN_NAME_PUB_DATE + " desc");
    }

    @Override
    public void onDestroy() {
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public int getCount() {
        Log.d(TAG, "getCount: ");
        if (cursor != null) {
            return cursor.getCount();
        } else {
            return 0;
        }
    }

    /**
     * Similar to getView of Adapter where instead of View
     * we return RemoteViews
     */
    @Override
    public RemoteViews getViewAt(int position) {
        cursor.moveToPosition(position);

        String title = cursor.getString(COLUMN_TITLE);
        String imageUrl = cursor.getString(COLUMN_IMAGE_URL);
        String link = cursor.getString(COLUMN_LINK);

        Log.d(TAG, "getViewAt: " + position + " title = " + title);

        final RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget_list_item);
        remoteView.setTextViewText(R.id.item_title, title);
        try {
            Bitmap b = Picasso.with(context).load(imageUrl).get();
            remoteView.setImageViewBitmap(R.id.item_image, b);
        } catch (IOException e) {
            Log.e(TAG, "image load error", e);
        }

        remoteView.setOnClickFillInIntent(R.id.item, new Intent());

        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if (cursor != null) {
            return cursor.getInt(COLUMN_ID);
        } else {
            return position;
        }
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}