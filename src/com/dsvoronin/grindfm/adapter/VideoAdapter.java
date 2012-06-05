package com.dsvoronin.grindfm.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.activity.HttpListActivity;
import com.dsvoronin.grindfm.model.Video;
import com.dsvoronin.grindfm.util.ImageManager;
import com.dsvoronin.grindfm.util.StringUtil;

public class VideoAdapter extends BaseListAdapter<Video> {

    public VideoAdapter(HttpListActivity<Video> context, int layoutId) {
        super(context, layoutId);
    }

    @Override
    protected View setupView(View view, Video currentItem, int index) {
        ImageView thumb = (ImageView) view.findViewById(R.id.thumb);
        ImageManager.getInstance().displayImage(thumb, getItem(index).getThumb().getLowQuality());

        TextView title = (TextView) view.findViewById(R.id.videoTitle);
        title.setText(getItem(index).getTitle());

        TextView date = (TextView) view.findViewById(R.id.videoDate);
        date.setText(StringUtil.formatDate(getItem(index).getDate()));

        return view;
    }
}
