package com.dsvoronin.grindfm.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.activity.HttpListActivity;
import com.dsvoronin.grindfm.model.NewsItem;
import com.dsvoronin.grindfm.util.ImageManager;

public class NewsAdapter extends BaseListAdapter<NewsItem> {

    public NewsAdapter(HttpListActivity<NewsItem> context, int layoutId) {
        super(context, layoutId);
    }

    @Override
    protected View setupView(View view, NewsItem currentItem, int index) {
        TextView newsTitle = (TextView) view.findViewById(R.id.news_title);
        newsTitle.setText(currentItem.getTitle());

        ImageView newsImage = (ImageView) view.findViewById(R.id.news_image);
        String imageSrc = currentItem.getThumbImage();
        if (imageSrc != null) {
            ImageManager.getInstance().displayImage(newsImage, imageSrc);
        } else {
            ImageManager.getInstance().displayImage(newsImage, R.drawable.goha_icon);
        }

        TextView newsLead = (TextView) view.findViewById(R.id.news_lead);
        newsLead.setText(currentItem.getTextDescription());
        return view;
    }
}
