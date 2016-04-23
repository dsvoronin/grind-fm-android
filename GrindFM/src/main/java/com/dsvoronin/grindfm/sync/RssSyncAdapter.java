package com.dsvoronin.grindfm.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.dsvoronin.grindfm.App;
import com.dsvoronin.grindfm.network.GrindService;
import com.dsvoronin.grindfm.network.rss.Article;
import com.dsvoronin.grindfm.network.rss.RSS;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class RssSyncAdapter extends AbstractThreadedSyncAdapter {

    private final String TAG = "RssSyncAdapter";

    private final GrindService grindService;

    public RssSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        grindService = App.fromContext(context).getGrindService();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "onPerformSync");
        try {

            Call<RSS> call = grindService.getGrindFeed();
            Response<RSS> response = call.execute();
            RSS rss = response.body();
            List<Article> articles = rss.getChannel().getArticleList();

            for (Article article : articles) {
                Log.d(TAG, article.getTitle() + " : " + article.getGuid());
            }

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
