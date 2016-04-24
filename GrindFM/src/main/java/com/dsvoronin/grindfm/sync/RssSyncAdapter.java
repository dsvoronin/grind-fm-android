package com.dsvoronin.grindfm.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.dsvoronin.grindfm.App;
import com.dsvoronin.grindfm.entities.Article;
import com.dsvoronin.grindfm.entities.NewsItem;
import com.dsvoronin.grindfm.entities.RSS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

import static com.dsvoronin.grindfm.sync.GrindProvider.Contract.Entry.COLUMN_NAME_DESCRIPTION;
import static com.dsvoronin.grindfm.sync.GrindProvider.Contract.Entry.COLUMN_NAME_ENTRY_ID;
import static com.dsvoronin.grindfm.sync.GrindProvider.Contract.Entry.COLUMN_NAME_FORMATTED_DATE;
import static com.dsvoronin.grindfm.sync.GrindProvider.Contract.Entry.COLUMN_NAME_IMAGE_URL;
import static com.dsvoronin.grindfm.sync.GrindProvider.Contract.Entry.COLUMN_NAME_LINK;
import static com.dsvoronin.grindfm.sync.GrindProvider.Contract.Entry.COLUMN_NAME_PUB_DATE;
import static com.dsvoronin.grindfm.sync.GrindProvider.Contract.Entry.COLUMN_NAME_TITLE;
import static com.dsvoronin.grindfm.sync.GrindProvider.Contract.Entry.CONTENT_URI;

public class RssSyncAdapter extends AbstractThreadedSyncAdapter {

    private final String TAG = "RssSyncAdapter";

    private final GrindService grindService;

    /**
     * Content resolver, for performing database operations.
     */
    private final ContentResolver contentResolver;

    // Constants representing column positions from PROJECTION.
    public static final int COLUMN_ID = 0;
    public static final int COLUMN_ENTRY_ID = 1;

    public RssSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        grindService = App.fromContext(context).getGrindService();
        contentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "onPerformSync");
        try {
            Call<RSS> call = grindService.getGrindFeed();
            Response<RSS> response = call.execute();
            RSS rss = response.body();
            List<Article> incomingRawEntries = rss.getChannel().getArticleList();

            List<NewsItem> incomingEntries = new ArrayList<>(incomingRawEntries.size());

            for (Article article : incomingRawEntries) {
                incomingEntries.add(fromArticle(article));
            }

            ArrayList<ContentProviderOperation> batch = new ArrayList<>();

            Log.i(TAG, "Parsing complete. Found " + incomingEntries.size() + " entries");

            Map<Integer, NewsItem> incomingEntriesMap = toMap(incomingEntries);

            Log.i(TAG, "Fetching local entries for merge");

            Cursor localEntriesCursor = getLocalEntries();

            Log.i(TAG, "Found " + localEntriesCursor.getCount() + " local entries. Computing merge solution...");

            updateLocalEntryIfNeeded(localEntriesCursor, incomingEntriesMap, batch, syncResult);

            addNewItems(incomingEntriesMap, batch, syncResult);

            Log.i(TAG, "Merge solution ready. Applying batch update");
            contentResolver.applyBatch(GrindProvider.Contract.CONTENT_AUTHORITY, batch);
            contentResolver.notifyChange(
                    CONTENT_URI, // URI where data was modified
                    null,                           // No local observer
                    false);                         // IMPORTANT: Do not sync to network
            // This sample doesn't support uploads, but if *your* code does, make sure you set
            // syncToNetwork=false in the line above to prevent duplicate syncs.

        } catch (IOException ioe) {
            Log.e(TAG, "Error reading from network", ioe);
            syncResult.stats.numIoExceptions++;
            return;
        } catch (RuntimeException re) {
            Log.e(TAG, "Error parsing data", re);
            syncResult.stats.numParseExceptions++;
            return;
        } catch (RemoteException | OperationApplicationException e) {
            Log.e(TAG, "Error updating database: " + e.toString());
            syncResult.databaseError = true;
            return;
        }

        Log.d(TAG, "Network synchronization complete");
    }

    /**
     * For easy comparison operations we need id to article map
     */
    private Map<Integer, NewsItem> toMap(List<NewsItem> articles) {
        Map<Integer, NewsItem> result = new HashMap<>();

        for (NewsItem newsItem : articles) {
            result.put(newsItem.getItemId(), newsItem);
        }

        return result;
    }

    private Cursor getLocalEntries() {
        Uri uri = CONTENT_URI;
        Cursor c = contentResolver.query(uri, null, null, null, null);
        assert c != null;
        return c;
    }

    /**
     * Schedule local entry deletion
     *
     * @param dbId       entry dbId to delete
     * @param batch      batch for optimization
     * @param syncResult syncResult to update affected meta info
     */
    private void removeLocalEntry(int dbId,
                                  ArrayList<ContentProviderOperation> batch,
                                  SyncResult syncResult) {

        Uri deleteUri = CONTENT_URI.buildUpon().appendPath(Integer.toString(dbId)).build();
        Log.i(TAG, "Scheduling delete: " + deleteUri);
        batch.add(ContentProviderOperation.newDelete(deleteUri).build());
        syncResult.stats.numDeletes++;
    }

    private void updateLocalEntryIfNeeded(Cursor localEntriesCursor,
                                          Map<Integer, NewsItem> incomingMap,
                                          ArrayList<ContentProviderOperation> batch,
                                          SyncResult syncResult) {

        int id;
        int entryId;

        while (localEntriesCursor.moveToNext()) {
            syncResult.stats.numEntries++;
            id = localEntriesCursor.getInt(COLUMN_ID);
            entryId = localEntriesCursor.getInt(COLUMN_ENTRY_ID);

            NewsItem match = incomingMap.get(entryId);
            if (match != null) {
                // Entry exists. Remove from entry map to prevent insert later.
                incomingMap.remove(entryId);
                // Check to see if the entry needs to be updated
                Uri existingUri = CONTENT_URI.buildUpon().appendPath(Integer.toString(id)).build();

                NewsItem localNewsItem = fromCursor(localEntriesCursor);

                if (!match.equals(localNewsItem)) {
                    // Update existing record
                    Log.i(TAG, "Scheduling update: " + existingUri);
                    batch.add(fillOperation(ContentProviderOperation.newUpdate(existingUri), match).build());
                    syncResult.stats.numUpdates++;
                } else {
                    Log.i(TAG, "No action: " + existingUri);
                }
            } else {
                // Entry doesn't exist. Remove it from the database.
                removeLocalEntry(id, batch, syncResult);
            }
        }
        localEntriesCursor.close();
    }

    private void addNewItems(Map<Integer, NewsItem> incomingArticleMap,
                             ArrayList<ContentProviderOperation> batch,
                             SyncResult syncResult) {

        for (NewsItem newsItem : incomingArticleMap.values()) {
            Log.i(TAG, "Scheduling insert: entry_id=" + newsItem.getItemId());
            batch.add(fillOperation(ContentProviderOperation.newInsert(CONTENT_URI), newsItem).build());
            syncResult.stats.numInserts++;
        }
    }

    private NewsItem fromCursor(Cursor c) {
        return new NewsItem(
                c.getInt(c.getColumnIndex(COLUMN_NAME_ENTRY_ID)),
                c.getString(c.getColumnIndex(COLUMN_NAME_TITLE)),
                c.getString(c.getColumnIndex(COLUMN_NAME_DESCRIPTION)),
                c.getLong(c.getColumnIndex(COLUMN_NAME_PUB_DATE)),
                c.getString(c.getColumnIndex(COLUMN_NAME_IMAGE_URL)),
                c.getString(c.getColumnIndex(COLUMN_NAME_LINK)),
                c.getString(c.getColumnIndex(COLUMN_NAME_FORMATTED_DATE)));
    }

    private NewsItem fromArticle(Article article) {
        Mapper mapper = new Mapper(getContext(), article);
        return new NewsItem(mapper.getId(),
                article.getTitle(),
                mapper.getPureText(),
                mapper.getPubDate(),
                mapper.getImageUrl(),
                article.getLink(),
                mapper.getFormattedDate());
    }

    private ContentProviderOperation.Builder fillOperation(ContentProviderOperation.Builder operation, NewsItem newsItem) {
        return operation
                .withValue(COLUMN_NAME_ENTRY_ID, newsItem.getItemId())
                .withValue(COLUMN_NAME_TITLE, newsItem.getTitle())
                .withValue(COLUMN_NAME_LINK, newsItem.getLink())
                .withValue(COLUMN_NAME_PUB_DATE, newsItem.getPubDate())
                .withValue(COLUMN_NAME_DESCRIPTION, newsItem.getDescription())
                .withValue(COLUMN_NAME_IMAGE_URL, newsItem.getImageUrl())
                .withValue(COLUMN_NAME_FORMATTED_DATE, newsItem.getFormattedDate());
    }
}
