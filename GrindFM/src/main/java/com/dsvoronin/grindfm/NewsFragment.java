package com.dsvoronin.grindfm;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.dsvoronin.grindfm.sync.GrindProvider;

public class NewsFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "NewsFragment";

    /**
     * Cursor adapter for controlling ListView results.
     */
    private SimpleCursorAdapter adapter;

    /**
     * Projection for querying the content provider.
     */
    private static final String[] PROJECTION = new String[]{
            GrindProvider.Contract.Entry._ID,
            GrindProvider.Contract.Entry.COLUMN_NAME_TITLE,
            GrindProvider.Contract.Entry.COLUMN_NAME_LINK,
            GrindProvider.Contract.Entry.COLUMN_NAME_PUBLISHED
    };

    // Column indexes. The index of a column in the Cursor is the same as its relative position in
    // the projection.
    /**
     * Column index for _ID
     */
    private static final int COLUMN_ID = 0;
    /**
     * Column index for title
     */
    private static final int COLUMN_TITLE = 1;
    /**
     * Column index for link
     */
    private static final int COLUMN_URL_STRING = 2;
    /**
     * Column index for published
     */
    private static final int COLUMN_PUBLISHED = 3;

    /**
     * List of Cursor columns to read from when preparing an adapter to populate the ListView.
     */
    private static final String[] FROM_COLUMNS = new String[]{
            GrindProvider.Contract.Entry.COLUMN_NAME_TITLE,
            GrindProvider.Contract.Entry.COLUMN_NAME_PUBLISHED
    };

    /**
     * List of Views which will be populated by Cursor data.
     */
    private static final int[] TO_FIELDS = new int[]{
            android.R.id.text1,
            android.R.id.text2};


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new SimpleCursorAdapter(
                getActivity(),       // Current context
                android.R.layout.simple_list_item_activated_2,  // Layout for individual rows
                null,                // Cursor
                FROM_COLUMNS,        // Cursor columns to use
                TO_FIELDS,           // Layout fields to use
                0                    // No flags
        );
        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {
                if (i == COLUMN_PUBLISHED) {
                    // Convert timestamp to human-readable date
                    Time t = new Time();
                    t.set(cursor.getLong(i));
                    ((TextView) view).setText(t.format("%Y-%m-%d %H:%M"));
                    return true;
                } else {
                    // Let SimpleCursorAdapter handle other fields automatically
                    return false;
                }
            }
        });
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
                GrindProvider.Contract.Entry.CONTENT_URI, // URI
                PROJECTION,                // Projection
                null,                           // Selection
                null,                           // Selection args
                GrindProvider.Contract.Entry.COLUMN_NAME_PUBLISHED + " desc"); // Sort
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
        String articleUrlString = c.getString(COLUMN_URL_STRING);
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
}
