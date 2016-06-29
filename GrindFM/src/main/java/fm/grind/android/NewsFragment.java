package fm.grind.android;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import fm.grind.android.utils.BetterViewAnimator;
import fm.grind.android.utils.CursorRecyclerAdapter;

import static android.provider.BaseColumns._ID;
import static fm.grind.android.sync.GrindProvider.Contract.Entry.COLUMN_NAME_FORMATTED_DATE;
import static fm.grind.android.sync.GrindProvider.Contract.Entry.COLUMN_NAME_IMAGE_URL;
import static fm.grind.android.sync.GrindProvider.Contract.Entry.COLUMN_NAME_LINK;
import static fm.grind.android.sync.GrindProvider.Contract.Entry.COLUMN_NAME_PUB_DATE;
import static fm.grind.android.sync.GrindProvider.Contract.Entry.COLUMN_NAME_TITLE;
import static fm.grind.android.sync.GrindProvider.Contract.Entry.CONTENT_URI;

public class NewsFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "NewsFragment";

    private static final int NEWS_LOADER_ID = 0;

    private BetterViewAnimator viewAnimator;

    private RecyclerView recyclerView;

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
            COLUMN_NAME_FORMATTED_DATE
    };

    // Column indexes. The index of a column in the Cursor is the same as its relative position in
    // the projection. 0 = _ID
    private static final int COLUMN_TITLE = 1;
    private static final int COLUMN_LINK = 2;
    private static final int COLUMN_IMAGE_URL = 3;
    private static final int COLUMN_FORMATTED_DATE = 4;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        picasso = App.Companion.fromContext(context).getPicasso();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        viewAnimator = (BetterViewAnimator) view.findViewById(R.id.view_animator);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new NewsItemAdapter(getActivity(), null);
        recyclerView.setAdapter(adapter);
        getLoaderManager().initLoader(NEWS_LOADER_ID, null, this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getLoaderManager().destroyLoader(NEWS_LOADER_ID);
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
        if (cursor.getCount() > 0) {
            viewAnimator.setDisplayedChildId(R.id.recycler_view);
        } else {
            viewAnimator.setDisplayedChildId(R.id.loading);
        }
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

    private class NewsItemAdapter extends CursorRecyclerAdapter<NewsItemViewHolder> {

        private LayoutInflater layoutInflater;

        public NewsItemAdapter(Context context, Cursor cursor) {
            super(cursor);
            layoutInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public NewsItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new NewsItemViewHolder(layoutInflater.inflate(R.layout.news_item, parent, false));
        }

        @Override
        public void onBindViewHolderCursor(NewsItemViewHolder holder, Cursor cursor) {
            holder.bind(cursor, picasso);
        }
    }

    private void closeCursor() {
        Cursor cursor = adapter.getCursor();
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    private static class NewsItemViewHolder extends RecyclerView.ViewHolder {

        private Context context;

        private TextView titleView;

        private TextView pubDateView;

        private ImageView imageView;

        public NewsItemViewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            titleView = (TextView) itemView.findViewById(R.id.news_title);
            pubDateView = (TextView) itemView.findViewById(R.id.news_pub_date);
            imageView = (ImageView) itemView.findViewById(R.id.news_image);
        }

        public void bind(final Cursor cursor, Picasso picasso) {
            titleView.setText(cursor.getString(COLUMN_TITLE));
            pubDateView.setText(cursor.getString(COLUMN_FORMATTED_DATE));

            picasso.load(cursor.getString(COLUMN_IMAGE_URL))
                    .into(imageView);

            /**
             * Load an article in the default browser when selected by the user.
             */
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String articleUrlString = cursor.getString(COLUMN_LINK);

                    if (articleUrlString == null) {
                        Log.e(TAG, "Attempt to launch entry with null link");
                        return;
                    }

                    Log.i(TAG, "Opening URL: " + articleUrlString);
                    // Get a Uri object for the URL string
                    Uri articleURL = Uri.parse(articleUrlString);
                    Intent i = new Intent(Intent.ACTION_VIEW, articleURL);
                    context.startActivity(i);
                }
            });
        }
    }
}
