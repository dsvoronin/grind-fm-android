package com.dsvoronin.grindfm;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dsvoronin.grindfm.rss.RssItem;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends BaseAdapter {

    private List<RssItem> content = new ArrayList<RssItem>();

    private Context context;

    public NewsAdapter(Context context) {
        this.context = context;
    }

    private static String getImageUrl(String description) {
        String start = "img src=\"";
        String substring = description.substring(description.indexOf(start) + start.length());
        return substring.substring(0, substring.indexOf("\""));
    }

    private static String formatImageUrl(String badImageUrl) throws RuntimeException {
        String escaped = badImageUrl.replace(" ", "%20");
        URI uri = URI.create(escaped);
        return uri.toASCIIString();
    }

    @Override
    public int getCount() {
        return content.size();
    }

    @Override
    public RssItem getItem(int position) {
        return content.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void setContent(List<RssItem> content) {
        this.content.clear();
        this.content.addAll(content);
        notifyDataSetChanged();
    }

    @Nullable
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.news_item, null);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.news_title);
            holder.image = (ImageView) convertView.findViewById(R.id.news_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        RssItem item = getItem(position);
        holder.title.setText(item.getTitle());

        try {
            Picasso.with(context).load(formatImageUrl(getImageUrl(item.getDescription()))).into(holder.image);
        } catch (RuntimeException e) {
            //todo remove
        }

        return convertView;
    }

    private static class ViewHolder {
        private TextView title;
        private ImageView image;
    }
}