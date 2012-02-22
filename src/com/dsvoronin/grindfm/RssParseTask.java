package com.dsvoronin.grindfm;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.dsvoronin.R;
import com.dsvoronin.grindfm.util.SaxFeedParser;

import java.util.ArrayList;
import java.util.List;

public class RssParseTask extends AsyncTask<String, Void, List<Message>> {

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
    protected List<Message> doInBackground(String... strings) {
        try {
            SaxFeedParser parser = new SaxFeedParser(strings[0]);
            return parser.parse();
        } catch (Exception e) {
            Log.e(TAG, "Error while parsing RSS feed", e);
            return new ArrayList<Message>();
        }
    }

    @Override
    protected void onPostExecute(List<Message> messages) {
        if (messages.size() > 0) {
            mAdapter.replace(messages);
            mAdapter.notifyDataSetChanged();
            progressDialog.dismiss();
        } else {
            Toast.makeText(mContext, "Не удалось загрузить новости", Toast.LENGTH_SHORT).show();
        }
    }
}
