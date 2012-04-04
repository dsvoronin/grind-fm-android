package com.dsvoronin.grindfm.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.model.NewsItem;
import com.dsvoronin.grindfm.util.ImageManager;

public class NewsAdapter extends BaseListAdapter<NewsItem> {

    public NewsAdapter(Activity activity) {
        super(activity);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = getInflater().inflate(R.layout.news_item, null);
        }

        NewsItem n = getItem(i);

        TextView newsTitle = (TextView) view.findViewById(R.id.news_title);
        newsTitle.setText(n.getTitle());

        ImageView newsImage = (ImageView) view.findViewById(R.id.news_image);
        String imageSrc = n.getThumbImage();
        if (imageSrc != null) {
            ImageManager.getInstance().displayImage(newsImage, imageSrc);
        } else {
            ImageManager.getInstance().displayImage(newsImage, R.drawable.goha_icon);
        }

        TextView newsLead = (TextView) view.findViewById(R.id.news_lead);
        newsLead.setText(n.getTextDescription());

        return view;
    }
}
