package com.dsvoronin.grindfm;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.dsvoronin.grindfm.cache.ImageCacheManager;
import com.dsvoronin.grindfm.network.GrindRequest;
import com.dsvoronin.grindfm.network.RequestManager;
import com.dsvoronin.grindfm.rss.RssFeed;
import com.dsvoronin.grindfm.rss.RssItem;
import com.dsvoronin.grindfm.rss.RssReader;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewsFragment extends ListFragment {

    private static final String TAG = "GrindFM.News";

    @Override
    public void onStart() {
        super.onStart();
        load();
    }

    private void load() {
        GrindRequest request = new GrindRequest(getString(R.string.rss_url), new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    RssFeed read = RssReader.read(s);
                    List<Map<String, String>> data = new ArrayList<Map<String, String>>();
                    for (RssItem rssItem : read.getRssItems()) {
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("title", rssItem.getTitle());

                        String description = rssItem.getDescription();

                        Log.d(TAG, "Description = " + description);

                        String start = "img src=\"";
                        String substring = description.substring(description.indexOf(start) + start.length());

                        Log.d(TAG, "substring = " + substring);

                        String nextQuote = "\"";
                        String imageSrc = substring.substring(0, substring.indexOf(nextQuote));

                        Log.d(TAG, "imageSrc = " + substring);

                        map.put("image", imageSrc);

                        map.put("description", Html.fromHtml(description).toString());

                        data.add(map);
                    }

                    Activity activity = getActivity();
                    if (activity != null) {
                        setListAdapter(new NewsAdapter(activity, data, R.layout.news_item, new String[]{"title", "image", "description"}, new int[]{R.id.news_title, R.id.news_image, R.id.news_description}));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing news rss", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e(TAG, "Error loading news rss", volleyError);
            }
        }
        );
        RequestManager.getRequestQueue().add(request);
    }

    private class NewsAdapter extends SimpleAdapter {

        /**
         * Constructor
         *
         * @param context  The context where the View associated with this SimpleAdapter is running
         * @param data     A List of Maps. Each entry in the List corresponds to one row in the list. The
         *                 Maps contain the data for each row, and should include all the entries specified in
         *                 "from"
         * @param resource Resource identifier of a view layout that defines the views for this list
         *                 item. The layout file should include at least those named views defined in "to"
         * @param from     A list of column names that will be added to the Map associated with each
         *                 item.
         * @param to       The views that should display column in the "from" parameter. These should all be
         *                 TextViews. The first N views in this list are given the values of the first N columns
         *                 in the from parameter.
         */
        public NewsAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        @Override
        public void setViewImage(@NotNull ImageView v, String value) {
            if (v instanceof NetworkImageView) {
                NetworkImageView imageView = (NetworkImageView) v;
                Log.d(TAG, "value = " + value);
                if (value != null) {
                    try {
                        String escaped = value.replace(" ", "%20");
                        Log.d(TAG, "Escaped = " + escaped);

                        URI uri = URI.create(escaped);
                        String ascii = uri.toASCIIString();
                        Log.d(TAG, "ascii = " + ascii);

                        imageView.setImageUrl(ascii, ImageCacheManager.getInstance().getImageLoader());
                    } catch (NullPointerException e) {
                        Log.e(TAG, "Image error", e);
                    } catch (IllegalArgumentException sye) {
                        Log.e(TAG, "Bad url", sye);
                    }
                }
            } else {
                Log.d(TAG, "Image is not volley.NetworkImageView");
            }
        }
    }
}
