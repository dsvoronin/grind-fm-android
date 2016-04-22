package com.dsvoronin.grindfm.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

public class RssSyncAdapter extends AbstractThreadedSyncAdapter {

    private final String TAG = "RssSyncAdapter";

    private final AccountManager mAccountManager;

    public RssSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "onPerformSync");
        try {
            // Get the auth token for the current account
//            String authToken = mAccountManager.blockingGetAuthToken(account,
//                    AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, true);

            // Get shows from the remote server
//            List remoteTvShows = parseComService.getShows(authToken);

            // Get shows from the local storage
//            ArrayList localTvShows = new ArrayList();
//            Cursor curTvShows = provider.query(TvShowsContract.CONTENT_URI, null, null, null, null);
//            if (curTvShows != null) {
//                while (curTvShows.moveToNext()) {
//                    localTvShows.add(TvShow.fromCursor(curTvShows));
//                }
//                curTvShows.close();
//            }
            // TODO See what Local shows are missing on Remote

            // TODO See what Remote shows are missing on Local

            // TODO Updating remote tv shows

            // TODO Updating local tv shows

        } catch (Exception e) {
            Log.e(TAG, "Sync error", e);
        }
    }
}
