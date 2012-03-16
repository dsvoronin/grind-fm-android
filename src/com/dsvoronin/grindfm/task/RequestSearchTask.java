package com.dsvoronin.grindfm.task;

import android.content.Context;
import com.dsvoronin.grindfm.adapter.BaseListAdapter;
import com.dsvoronin.grindfm.model.RequestSong;
import com.dsvoronin.grindfm.util.RequestUtil;

import java.util.ArrayList;

public class RequestSearchTask extends BaseTask {

    public RequestSearchTask(Context mContext, BaseListAdapter mAdapter) {
        super(mContext, mAdapter);
    }

    @Override
    protected ArrayList processAsync(String url) throws Exception {
        return new ArrayList<RequestSong>(RequestUtil.search(url));
    }

    @Override
    protected void afterTaskActions() {

    }
}
