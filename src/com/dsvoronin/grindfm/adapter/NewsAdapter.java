package com.dsvoronin.grindfm.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.dsvoronin.R;
import com.dsvoronin.grindfm.model.NewsItem;

public class NewsAdapter extends BaseListAdapter<NewsItem> {

    public NewsAdapter(Activity activity) {
        super(activity);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = getInflater().inflate(R.layout.news_item, null);
        }

        TextView title = (TextView) view.findViewById(R.id.dialogTitle);
        title.setText(getItem(i).getTitle());

        TextView date = (TextView) view.findViewById(R.id.date);
        date.setText(getItem(i).getDate());

        return view;
    }
}
