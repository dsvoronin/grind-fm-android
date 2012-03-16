package com.dsvoronin.grindfm.task;

import android.content.Context;
import com.dsvoronin.grindfm.adapter.BaseListAdapter;
import com.dsvoronin.grindfm.model.Video;
import com.dsvoronin.grindfm.util.YouTubeUtil;

import java.util.ArrayList;

public class VideoTask extends BaseTask {

    public VideoTask(Context mContext, BaseListAdapter mAdapter) {
        super(mContext, mAdapter);
    }

    @Override
    protected ArrayList processAsync(String url) throws Exception {
        YouTubeUtil youTubeUtil = new YouTubeUtil();
        return new ArrayList<Video>(youTubeUtil.getVideos());
    }

    @Override
    protected void afterTaskActions() {
    }
}
