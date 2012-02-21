package com.dsvoronin.grindfm;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.dsvoronin.R;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends BaseAdapter {

    private Activity mContext;

    private List<Message> mContent;

    public NewsAdapter(Activity mContext) {
        this.mContext = mContext;
        mContent = new ArrayList<Message>();
    }

    public void add(Message message) {
        mContent.add(message);
    }

    public void replace(List<Message> messageList) {
        mContent.clear();
        mContent.addAll(messageList);
    }

    @Override
    public int getCount() {
        return mContent.size();
    }

    @Override
    public Object getItem(int i) {
        return mContent.get(i);
    }

    @Override
    @Deprecated
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = mContext.getLayoutInflater().inflate(R.layout.news_item, null);
        }

        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(mContent.get(i).getTitle());

        return view;
    }
}
