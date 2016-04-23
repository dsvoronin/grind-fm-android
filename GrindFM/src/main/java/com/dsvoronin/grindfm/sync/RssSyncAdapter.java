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
import com.dsvoronin.grindfm.network.GrindService;
import com.dsvoronin.grindfm.network.rss.Article;
import com.dsvoronin.grindfm.network.rss.RSS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class RssSyncAdapter extends AbstractThreadedSyncAdapter {

    private final String TAG = "RssSyncAdapter";

    private final GrindService grindService;

    /**
     * Content resolver, for performing database operations.
     */
    private final ContentResolver contentResolver;

    /**
     * Project used when querying content provider. Returns all known fields.
     */
    private static final String[] PROJECTION = new String[]{
            GrindProvider.Contract.Entry._ID,
            GrindProvider.Contract.Entry.COLUMN_NAME_ENTRY_ID,
            GrindProvider.Contract.Entry.COLUMN_NAME_TITLE,
            GrindProvider.Contract.Entry.COLUMN_NAME_LINK,
            GrindProvider.Contract.Entry.COLUMN_NAME_PUBLISHED};

    // Constants representing column positions from PROJECTION.
    public static final int COLUMN_ID = 0;
    public static final int COLUMN_ENTRY_ID = 1;
    public static final int COLUMN_TITLE = 2;
    public static final int COLUMN_LINK = 3;
    public static final int COLUMN_PUBLISHED = 4;

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
            List<Article> incomingEntries = rss.getChannel().getArticleList();

            ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

            Log.i(TAG, "Parsing complete. Found " + incomingEntries.size() + " entries");

            Map<Integer, Article> incomingEntriesMap = toMap(incomingEntries);

            Log.i(TAG, "Fetching local entries for merge");

            Cursor localEntriesCursor = getLocalEntries();

            Log.i(TAG, "Found " + localEntriesCursor.getCount() + " local entries. Computing merge solution...");

            updateLocalEntryIfNeeded(localEntriesCursor, incomingEntriesMap, batch, syncResult);

            addNewItems(incomingEntriesMap, batch, syncResult);

            Log.i(TAG, "Merge solution ready. Applying batch update");
            contentResolver.applyBatch(GrindProvider.Contract.CONTENT_AUTHORITY, batch);
            contentResolver.notifyChange(
                    GrindProvider.Contract.Entry.CONTENT_URI, // URI where data was modified
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
     * guid is a weird string like: "1653 at http://www.grind.fm"
     *
     * @return we need 1653 here
     */
    private int extractId(Article article) throws NumberFormatException {
        String guid = article.getGuid();
        return Integer.parseInt(guid.substring(0, guid.indexOf(' ')));
    }

    /**
     * For easy comparison operations we need id to article map
     */
    private Map<Integer, Article> toMap(List<Article> articles) {
        Map<Integer, Article> result = new HashMap<>();

        for (Article article : articles) {
            result.put(extractId(article), article);
        }

        return result;
    }

    private Cursor getLocalEntries() {
        Uri uri = GrindProvider.Contract.Entry.CONTENT_URI;
        Cursor c = contentResolver.query(uri, PROJECTION, null, null, null);
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
    private void removeLocalEntry(int dbId, ArrayList<ContentProviderOperation> batch, SyncResult syncResult) {
        Uri deleteUri = GrindProvider.Contract.Entry.CONTENT_URI.buildUpon()
                .appendPath(Integer.toString(dbId)).build();
        Log.i(TAG, "Scheduling delete: " + deleteUri);
        batch.add(ContentProviderOperation.newDelete(deleteUri).build());
        syncResult.stats.numDeletes++;
    }

    private void updateLocalEntryIfNeeded(Cursor localEntriesCursor, Map<Integer, Article> incomingMap, ArrayList<ContentProviderOperation> batch, SyncResult syncResult) {

        int id;
        int entryId;
        String title;
        String link;

        while (localEntriesCursor.moveToNext()) {
            syncResult.stats.numEntries++;
            id = localEntriesCursor.getInt(COLUMN_ID);
            entryId = localEntriesCursor.getInt(COLUMN_ENTRY_ID);
            title = localEntriesCursor.getString(COLUMN_TITLE);
            link = localEntriesCursor.getString(COLUMN_LINK);
            Article match = incomingMap.get(entryId);
            if (match != null) {
                // Entry exists. Remove from entry map to prevent insert later.
                incomingMap.remove(entryId);
                // Check to see if the entry needs to be updated
                Uri existingUri = GrindProvider.Contract.Entry.CONTENT_URI.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                if ((match.getTitle() != null && !match.getTitle().equals(title)) ||
                        (match.getLink() != null && !match.getLink().equals(link))) {
                    // Update existing record
                    Log.i(TAG, "Scheduling update: " + existingUri);
                    batch.add(ContentProviderOperation.newUpdate(existingUri)
                            .withValue(GrindProvider.Contract.Entry.COLUMN_NAME_TITLE, match.getTitle())
                            .withValue(GrindProvider.Contract.Entry.COLUMN_NAME_LINK, match.getLink())
                            .build());
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

    private void addNewItems(Map<Integer, Article> incomingArticleMap, ArrayList<ContentProviderOperation> batch, SyncResult syncResult) {
        for (Article article : incomingArticleMap.values()) {
            int articleId = extractId(article);
            Log.i(TAG, "Scheduling insert: entry_id=" + articleId);
            batch.add(ContentProviderOperation.newInsert(GrindProvider.Contract.Entry.CONTENT_URI)
                    .withValue(GrindProvider.Contract.Entry.COLUMN_NAME_ENTRY_ID, articleId)
                    .withValue(GrindProvider.Contract.Entry.COLUMN_NAME_TITLE, article.getTitle())
                    .withValue(GrindProvider.Contract.Entry.COLUMN_NAME_LINK, article.getLink())
                    .build());
            syncResult.stats.numInserts++;
        }
    }
}
