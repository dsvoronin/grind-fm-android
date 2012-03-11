package com.dsvoronin.grindfm.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.adapter.BaseListAdapter;

import java.util.ArrayList;

public abstract class BaseTask extends AsyncTask<String, Void, ArrayList> {

    private static final String TAG = BaseTask.class.getSimpleName();

    private Context mContext;
    private BaseListAdapter mAdapter;
    private ImageView mProgress;

    public BaseTask(Context mContext, BaseListAdapter mAdapter) {
        this.mContext = mContext;
        this.mAdapter = mAdapter;
    }

    public void setProgress(ImageView mProgress) {
        this.mProgress = mProgress;
    }

    @Override
    protected void onPreExecute() {
        if (mProgress != null) {
            mProgress.setImageResource(R.drawable.cat_logo);
            mProgress.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fading));
            mProgress.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected ArrayList doInBackground(String... url) {
        try {
            return processAsync(url[0]);
        } catch (Exception e) {
            Log.e(TAG, "Error in async task", e);
        }
        return new ArrayList<Object>();
    }

    @Override
    protected void onPostExecute(ArrayList list) {
        if (mProgress != null) {
            mProgress.setAnimation(null);
        }

        if (list.size() > 0) {
            mAdapter.replaceContent(list);
            mAdapter.notifyDataSetChanged();
            if (mProgress != null) {
                mProgress.setVisibility(View.GONE);
            }
        } else {
            if (mProgress != null) {
                mProgress.setImageResource(R.drawable.cat_logo_x_x);
            }
        }
    }

    protected abstract ArrayList processAsync(String url) throws Exception;
}
