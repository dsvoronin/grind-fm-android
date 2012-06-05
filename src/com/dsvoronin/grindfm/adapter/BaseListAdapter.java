package com.dsvoronin.grindfm.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.dsvoronin.grindfm.activity.HttpListActivity;

import java.util.ArrayList;

/**
 * User: dsvoronin
 * Date: 02.06.12
 * Time: 22:46
 * Базовый адаптер, работающий с arraylist
 */
public abstract class BaseListAdapter<T> extends BaseAdapter {

    /**
     * активите, где будет отображаться список
     */
    private HttpListActivity<T> context;

    /**
     * layout для отображения элемента списка
     */
    private int layoutId;

    /**
     * список, который будет отображаться в активити
     */
    private ArrayList<T> content;

    public BaseListAdapter(HttpListActivity<T> context, int layoutId) {
        this.context = context;
        this.layoutId = layoutId;
        this.content = new ArrayList<T>();
    }

    public BaseListAdapter(HttpListActivity<T> context, int layoutId, ArrayList<T> content) {
        this.context = context;
        this.layoutId = layoutId;
        this.content = content;
    }

    /**
     * добавить список к уже существующему контенту
     *
     * @param list +контент
     */
    public void addListToExistingContent(ArrayList<T> list) {
        content.addAll(list);
        notifyDataSetChanged();
    }

    /**
     * заменить текущий контент новым
     *
     * @param list новый контент
     */
    public void replaceContent(ArrayList<T> list) {
        content.clear();
        content.addAll(list);
        notifyDataSetChanged();
    }

    /**
     * @return имеется ли контент?
     */
    public boolean hasContent() {
        return content != null && content.size() > 0;
    }

    /**
     * удалить текущий контент
     */
    public void clear() {
        content.clear();
    }

    /**
     * добавить один элемент
     * !!! Использовать только для добавлении отдельного элемента, не в цикле. для списка есть методы
     * addListToExistingContent
     * replaceContent
     *
     * @param object элемент
     */
    public void add(T object) {
        content.add(object);
        notifyDataSetChanged();
    }

    /**
     * @return текущий контент
     */
    public ArrayList<T> getContent() {
        return content;
    }

    /**
     * @return базовая активность
     */
    public HttpListActivity<T> getContext() {
        return context;
    }

    /**
     * @return размер текущего контента (кол-во элементов)
     */
    @Override
    public int getCount() {
        return content.size();
    }

    /**
     * получить элемент по индексу
     *
     * @param index индекс
     * @return элемент
     */
    @Override
    public T getItem(int index) {
        return content.get(index);
    }

    /**
     * получить айди элемента (используем hashcode)
     *
     * @param index индекс элемента
     * @return айди (.hashCode())
     */
    @Override
    public long getItemId(int index) {
        return getItem(index).hashCode();
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = context.getLayoutInflater().inflate(layoutId, null);
        }
        return setupView(view, getItem(index), index);
    }

    protected abstract View setupView(View view, T currentItem, int index);
}
