package com.dsvoronin.grindfm.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.model.NewsItem;
import com.dsvoronin.grindfm.util.ImageManager;
import org.jsoup.Jsoup;

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
        newsTitle.setText(Jsoup.parse(n.getTitle()).text());

        String imageSrc;
        try {
            imageSrc = Jsoup.parse(n.getDescription()).select("img").first().absUrl("src");
        } catch (Exception e) {
            Log.e("NewsAdapter", "No image", e);
            imageSrc = null;
        }
        if (imageSrc != null) {
            ImageView newsImage = (ImageView) view.findViewById(R.id.news_image);
            ImageManager.getInstance().displayImage(newsImage, imageSrc);
        }

        TextView newsLead = (TextView) view.findViewById(R.id.news_lead);
        newsLead.setText(Jsoup.parse(n.getDescription()).text());

        return view;
    }
}
