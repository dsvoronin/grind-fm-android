package com.dsvoronin.grindfm.task;

import android.content.Context;
import com.dsvoronin.grindfm.adapter.BaseListAdapter;

import java.util.ArrayList;

public class RequestSearchTask extends BaseTask {

    public RequestSearchTask(Context mContext, BaseListAdapter mAdapter) {
        super(mContext, mAdapter);
    }

    @Override
    protected ArrayList processAsync(String url) throws Exception {
        return null;
    }
}
