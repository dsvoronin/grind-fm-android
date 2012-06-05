package com.dsvoronin.grindfm.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.model.NewsItem;
import com.dsvoronin.grindfm.util.ImageManager;
import com.dsvoronin.grindfm.util.StringUtil;

/**
 * User: dsvoronin
 * Date: 04.04.12
 * Time: 11:37
 */
public class NewsDetailsActivity extends BaseActivity {

    public static final String INTENT_EXTRA = "news-detail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_details);

        NewsItem newsItem = (NewsItem) getIntent().getSerializableExtra(INTENT_EXTRA);

        TextView newsTitle = (TextView) findViewById(R.id.news_details_title);
        newsTitle.setText(newsItem.getTitle());

        if (newsItem.getThumbImage() != null) {
            ImageManager.getInstance().displayImage(findViewById(R.id.news_details_image), newsItem.getThumbImage());
        } else {
            ImageManager.getInstance().displayImage(findViewById(R.id.news_details_image), R.drawable.goha_icon);
        }

        TextView newsDescription = (TextView) findViewById(R.id.news_details_description);
        newsDescription.setText(newsItem.getTextDescription());

        TextView newsDate = (TextView) findViewById(R.id.news_details_date);
        newsDate.setText(newsItem.getDate());

        TextView newsLink = (TextView) findViewById(R.id.news_details_link);
        newsLink.setText(newsItem.getLink().toString());


        ImageView newsVideo = (ImageView) findViewById(R.id.news_details_video);
        RelativeLayout videoLayout = (RelativeLayout) findViewById(R.id.news_details_video_layout);
        videoLayout.setVisibility(View.GONE);
        if (newsItem.getVideo() != null) {
            String youtubeId = StringUtil.getYoutubeId(newsItem.getVideo());
            if (youtubeId != null) {
                videoLayout.setVisibility(View.VISIBLE);
                findViewById(R.id.news_detail_video_play).setOnClickListener(new OnVideoClickListener(newsItem.getVideo()));
                ImageManager.getInstance().displayImage(newsVideo, StringUtil.getThumbnail(youtubeId));
            }
        }
    }

    private class OnVideoClickListener implements View.OnClickListener {

        private String url;

        private OnVideoClickListener(String url) {
            this.url = url;
        }

        @Override
        public void onClick(View view) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        }
    }
}
