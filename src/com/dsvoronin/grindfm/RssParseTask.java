package com.dsvoronin.grindfm;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import com.dsvoronin.R;
import com.dsvoronin.grindfm.util.SaxFeedParser;

import java.util.List;

public class RssParseTask extends AsyncTask<String, Void, List<Message>> {

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
        SaxFeedParser parser = new SaxFeedParser(strings[0]);
        return parser.parse();
    }

    @Override
    protected void onPostExecute(List<Message> messages) {
        mAdapter.replace(messages);
        mAdapter.notifyDataSetChanged();
        progressDialog.dismiss();
    }
}
