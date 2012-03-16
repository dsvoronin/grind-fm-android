package com.dsvoronin.grindfm.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.model.RequestSong;

public class RequestSongAdapter extends BaseListAdapter<RequestSong> {

    public RequestSongAdapter(Activity activity) {
        super(activity);
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = getInflater().inflate(R.layout.request_item, null);
        }
        RequestSong song = getItem(index);

        TextView reqString = (TextView) view.findViewById(R.id.req_item_string);
        reqString.setText(song.getArtist() + " - " + song.getTitle());

        if (!song.isAvailable()) {
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
