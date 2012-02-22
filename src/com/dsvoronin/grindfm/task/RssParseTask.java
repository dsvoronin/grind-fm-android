package com.dsvoronin.grindfm.task;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.dsvoronin.R;
import com.dsvoronin.grindfm.adapter.NewsAdapter;
import com.dsvoronin.grindfm.model.NewsItem;
import com.dsvoronin.grindfm.util.SaxFeedParser;

import java.util.ArrayList;

public class RssParseTask extends AsyncTask<String, Void, ArrayList<NewsItem>> {

    private static final String TAG = RssParseTask.class.getSimpleName();

    private Activity mContext;
    private NewsAdapter mAdapter;
    private ProgressDialog progressDialog;

    public RssParseTask(Activity context, NewsAdapter adapter) {
        mContext = context;
        mAdapter = adapter;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(mContext.getString(R.string.loading));
        progressDialog.show();
    }

    @Override
    protected ArrayList<NewsItem> doInBackground(String... strings) {
        try {
            SaxFeedParser parser = new SaxFeedParser(strings[0]);
            return new ArrayList<NewsItem>(parser.parse());
        } catch (Exception e) {
            Log.e(TAG, "Error while parsing RSS feed", e);
            return new ArrayList<NewsItem>();
        }
    }

    @Override
    protected void onPostExecute(ArrayList<NewsItem> messages) {
        progressDialog.dismiss();
        if (messages.size() > 0) {
            mAdapter.replaceContent(messages);
            mAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(mContext, "Не удалось загрузить новости", Toast.LENGTH_SHORT).show();
        }
    }
}
