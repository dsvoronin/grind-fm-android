package com.dsvoronin.grindfm;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static android.provider.BaseColumns._ID;
import static com.dsvoronin.grindfm.sync.GrindProvider.Contract.Entry.COLUMN_NAME_IMAGE_URL;
import static com.dsvoronin.grindfm.sync.GrindProvider.Contract.Entry.COLUMN_NAME_LINK;
import static com.dsvoronin.grindfm.sync.GrindProvider.Contract.Entry.COLUMN_NAME_PUB_DATE;
import static com.dsvoronin.grindfm.sync.GrindProvider.Contract.Entry.COLUMN_NAME_TITLE;
import static com.dsvoronin.grindfm.sync.GrindProvider.Contract.Entry.CONTENT_URI;

public class NewsFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "NewsFragment";

    /**
     * Cursor adapter for controlling ListView results.
     */
    private NewsItemAdapter adapter;

    private Picasso picasso;

    /**
     * Projection for querying the content provider.
     */
    private static final String[] PROJECTION = new String[]{
            _ID,
            COLUMN_NAME_TITLE,
            COLUMN_NAME_LINK,
            COLUMN_NAME_IMAGE_URL,
            COLUMN_NAME_PUB_DATE
    };

    // Column indexes. The index of a column in the Cursor is the same as its relative position in
    // the projection.
    private static final int COLUMN_ID = 0;
    private static final int COLUMN_TITLE = 1;
    private static final int COLUMN_LINK = 2;
    private static final int COLUMN_IMAGE_URL = 3;
    private static final int COLUMN_PUB_DATE = 4;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        picasso = App.fromContext(context).getPicasso();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new NewsItemAdapter(getActivity(), null, false);
        setListAdapter(adapter);
        setEmptyText(getText(R.string.loading));
        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * Query the content provider for data.
     * <p/>
     * <p>Loaders do queries in a background thread. They also provide a ContentObserver that is
     * triggered when data in the content provider changes. When the sync adapter updates the
     * content provider, the ContentObserver responds by resetting the loader and then reloading
     * it.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // We only have one loader, so we can ignore the value of i.
        // (It'll be '0', as set in onCreate().)
        return new CursorLoader(getActivity(),  // Context
                CONTENT_URI, // URI
                PROJECTION,                // Projection
                null,                           // Selection
                null,                           // Selection args
                COLUMN_NAME_PUB_DATE + " desc"); // Sort
    }

    /**
     * Move the Cursor returned by the query into the ListView adapter. This refreshes the existing
     * UI with the data in the Cursor.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        adapter.changeCursor(cursor);
    }

    /**
     * Called when the ContentObserver defined for the content provider detects that data has
     * changed. The ContentObserver resets the loader, and then re-runs the loader. In the adapter,
     * set the Cursor value to null. This removes the reference to the Cursor, allowing it to be
     * garbage-collected.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        adapter.changeCursor(null);
    }

    /**
     * Load an article in the default browser when selected by the user.
     */
    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Get a URI for the selected item, then start an Activity that displays the URI. Any
        // Activity that filters for ACTION_VIEW and a URI can accept this. In most cases, this will
        // be a browser.

        // Get the item at the selected position, in the form of a Cursor.
        Cursor c = (Cursor) adapter.getItem(position);
        // Get the link to the article represented by the item.
        String articleUrlString = c.getString(COLUMN_LINK);
        if (articleUrlString == null) {
            Log.e(TAG, "Attempt to launch entry with null link");
            return;
        }

        Log.i(TAG, "Opening URL: " + articleUrlString);
        // Get a Uri object for the URL string
        Uri articleURL = Uri.parse(articleUrlString);
        Intent i = new Intent(Intent.ACTION_VIEW, articleURL);
        startActivity(i);
    }

    class NewsItemAdapter extends CursorAdapter {

        private LayoutInflater layoutInflater;

        private Locale locale = getResources().getConfiguration().locale;

        private SimpleDateFormat dbDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", locale);

        private SimpleDateFormat uiDateFormat = new SimpleDateFormat("EEE, d MMM HH:mm", locale);

        public NewsItemAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
            layoutInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return layoutInflater.inflate(R.layout.news_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView titleView = (TextView) view.findViewById(R.id.news_title);
            titleView.setText(cursor.getString(COLUMN_TITLE));

            TextView pubDateView = (TextView) view.findViewById(R.id.news_pub_date);
            pubDateView.setText(parseDate(cursor));

            ImageView imageView = (ImageView) view.findViewById(R.id.news_image);
            String imageUrl = cursor.getString(COLUMN_IMAGE_URL);
            picasso.load(imageUrl).into(imageView);
        }

        private String parseDate(Cursor cursor) {
            try {
                return uiDateFormat.format(dbDateFormat.parse(cursor.getString(COLUMN_PUB_DATE)));
            } catch (ParseException e) {
                return "";
            }
        }
    }
}
