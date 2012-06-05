package com.dsvoronin.grindfm.activity;

import android.os.Bundle;
import android.widget.EditText;
import com.dsvoronin.grindfm.R;

/**
 * User: dsvoronin
 * Date: 04.04.12
 * Time: 21:44
 */
public class MainActivity extends BaseActivity {

    private EditText searchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

//        searchText = (EditText) findViewById(R.id.search_query);
//
//        Button searchButton = (Button) findViewById(R.id.search_button);
//        searchButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String searchTextString = searchText.getText().toString();
//                if (searchTextString != null && !searchTextString.isEmpty()) {
//                    Intent intent = new Intent(MainActivity.this, RequestActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//                    intent.putExtra(RequestActivity.INTENT_EXTRA, searchTextString);
//                    startActivity(intent);
//                }
//            }
//        });
    }
}
