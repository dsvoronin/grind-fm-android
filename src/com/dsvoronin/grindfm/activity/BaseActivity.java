package com.dsvoronin.grindfm.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;
import com.dsvoronin.grindfm.R;
import com.dsvoronin.grindfm.view.MenuButton;

import java.util.HashMap;
import java.util.Map;

/**
 * User: dsvoronin
 * Date: 03.04.12
 * Time: 0:18
 */
public abstract class BaseActivity extends Activity implements MenuButton.Pickable {

    protected Map<Integer, MenuButton> menuButtons = new HashMap<Integer, MenuButton>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            MenuButton newsButton = (MenuButton) findViewById(R.id.menu_news);
            newsButton.setOnPickListener(new MenuButton.OnPickListener() {
                @Override
                public void onPick() {
                }
            });
            menuButtons.put(0, newsButton);
            menuButtons.get(0).performClick();

            TextView headerRunningString = (TextView) findViewById(R.id.header_running_string);
            headerRunningString.setSelected(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                return true;
            case R.id.settings:
                startActivity(new Intent(getBaseContext(), GrindPreferencesActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void pick(MenuButton pickedButton) {
        for (MenuButton button : menuButtons.values()) {
            if (!button.equals(pickedButton)) {
                button.setUnpicked();
            }
        }
    }
}
