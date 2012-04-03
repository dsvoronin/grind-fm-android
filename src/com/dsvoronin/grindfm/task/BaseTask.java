package com.dsvoronin.grindfm.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.adapter.BaseListAdapter;

import java.util.ArrayList;

public abstract class BaseTask extends AsyncTask<String, Void, ArrayList> {

    private static final String TAG = BaseTask.class.getSimpleName();

    private Context mContext;
    private BaseListAdapter mAdapter;
    private ImageView mProgress;
    private Button tryAgain;

    public BaseTask(Context mContext, BaseListAdapter mAdapter) {
        this.mContext = mContext;
        this.mAdapter = mAdapter;
    }

    public void setProgress(ImageView mProgress) {
        this.mProgress = mProgress;
    }

    public void setTryAgain(Button tryAgain) {
        this.tryAgain = tryAgain;
    }

    @Override
    protected void onPreExecute() {
        if (mProgress != null) {
            mProgress.setImageResource(R.drawable.cat_logo);
            mProgress.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fading));
            mProgress.setVisibility(View.VISIBLE);
        }
        if (tryAgain != null) {
            tryAgain.setVisibility(View.GONE);
        }
    }

    @Override
    protected ArrayList doInBackground(String... urls) {
        try {

            return processAsync(urls);

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
            if (tryAgain != null) {
                tryAgain.setVisibility(View.VISIBLE);
            }
        }

        afterTaskActions();
    }

    protected abstract ArrayList processAsync(String... urls) throws Exception;

    protected abstract void afterTaskActions();
}
