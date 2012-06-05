package com.dsvoronin.grindfm.activity;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.adapter.BaseListAdapter;
import com.dsvoronin.grindfm.task.BackgroundHttpTask;

import java.util.ArrayList;

/**
 * User: dsvoronin
 * Date: 05.06.12
 * Time: 6:46
 */
public abstract class HttpListActivity<T> extends BaseActivity {

    private BaseListAdapter<T> adapter;

    private ListView listView;

    private BackgroundHttpTask<T> httpTask = null;

    private ImageView progress;

    private Button tryAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());

        adapter = createAdapter();
        listView = findListView();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(defineItemClickListener());

        httpTask = (BackgroundHttpTask<T>) getLastNonConfigurationInstance();

        if (httpTask == null) {
            httpTask = createTask();
            httpTask.execute(getTaskParam());
        } else {
            httpTask.attach(this);
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        httpTask.detach();
        return httpTask;
    }

    public void displayProgress() {
        progress = (ImageView) findViewById(R.id.progress);
        tryAgain = (Button) findViewById(R.id.try_again);

        progress.setImageResource(R.drawable.cat_logo);
        progress.setAnimation(AnimationUtils.loadAnimation(this, R.anim.fading));
        progress.setVisibility(View.VISIBLE);

        tryAgain.setVisibility(View.GONE);
    }

    public void displayOk() {
        progress.setAnimation(null);
        progress.setVisibility(View.GONE);

        tryAgain.setVisibility(View.GONE);
    }

    public void displayGotError() {
        progress.setAnimation(null);
        progress.setImageResource(R.drawable.cat_logo_x_x);

        tryAgain.setVisibility(View.VISIBLE);
    }

    public void populateAdapter(ArrayList<T> data) {
        adapter.replaceContent(data);
        adapter.notifyDataSetChanged();
    }

    protected abstract int getLayoutId();

    protected abstract BaseListAdapter<T> createAdapter();

    protected abstract ListView findListView();

    protected abstract BackgroundHttpTask<T> createTask();

    protected abstract String getTaskParam();

    protected abstract AdapterView.OnItemClickListener defineItemClickListener();
}
