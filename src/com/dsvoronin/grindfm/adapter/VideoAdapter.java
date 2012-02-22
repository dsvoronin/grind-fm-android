package com.dsvoronin.grindfm.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.dsvoronin.R;
import com.dsvoronin.grindfm.model.Video;
import com.dsvoronin.grindfm.util.ImageManager;

public class VideoAdapter extends BaseListAdapter<Video> {

    public VideoAdapter(Activity activity) {
        super(activity);
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = getInflater().inflate(R.layout.news_item, null);
        }

        ImageView thumb = (ImageView) view.findViewById(R.id.thumb);
        ImageManager.getInstance().displayImage(thumb, getItem(index).getThumb());

        return view;
    }
}
