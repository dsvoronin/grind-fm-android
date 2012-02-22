package com.dsvoronin.grindfm.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/**
 * Базовый адаптер, работающий с array list'ом объектов
 *
 * @param <T>
 */
public abstract class BaseListAdapter<T> extends BaseAdapter {

    private ArrayList<T> mContent;

    private Activity mActivity;

    private LayoutInflater inflater;

    public BaseListAdapter(Activity activity) {
        mActivity = activity;
        mContent = new ArrayList<T>();

        init();
    }

    public BaseListAdapter(Activity activity, ArrayList<T> content) {
        mActivity = activity;
        mContent = content;

        init();
    }

    private void init() {
        inflater = mActivity.getLayoutInflater();
    }

    public void addListToExistingContent(ArrayList<T> list) {
        mContent.addAll(list);
    }

    public void replaceContent(ArrayList<T> list) {
        mContent.clear();
        mContent.addAll(list);
    }

    public boolean hasContent() {
        return mContent != null && mContent.size() > 0;
    }

    public void clear() {
        mContent.clear();
    }

    public void add(T object) {
        mContent.add(object);
    }

    public ArrayList<T> getContent() {
        return mContent;
    }

    @Override
    public int getCount() {
        return mContent.size();
    }

    @Override
    public T getItem(int index) {
        return mContent.get(index);
    }

    @Override
    public long getItemId(int index) {
        return getItem(index).hashCode();
    }

    protected Activity getActivity() {
        return mActivity;
    }

    protected LayoutInflater getInflater() {
        return inflater;
    }

    @Override
    public abstract View getView(int index, View view, ViewGroup viewGroup);
}
