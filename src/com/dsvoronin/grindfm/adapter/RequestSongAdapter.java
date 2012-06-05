package com.dsvoronin.grindfm.adapter;

import android.view.View;
import android.widget.TextView;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.activity.HttpListActivity;
import com.dsvoronin.grindfm.model.RequestSong;

public class RequestSongAdapter extends BaseListAdapter<RequestSong> {

    public RequestSongAdapter(HttpListActivity<RequestSong> context, int layoutId) {
        super(context, layoutId);
    }

    @Override
    protected View setupView(View view, RequestSong currentItem, int index) {
        TextView reqString = (TextView) view.findViewById(R.id.req_item_string);
        reqString.setText(currentItem.getArtist() + " - " + currentItem.getTitle());

        if (!currentItem.isAvailable()) {
            reqString.setBackgroundColor(R.color.disabled);
        } else {
            reqString.setBackgroundColor(android.R.color.transparent);
        }
        return view;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItem(position).isAvailable();
    }
}
