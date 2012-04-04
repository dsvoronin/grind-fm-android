package com.dsvoronin.grindfm.activity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.dsvoronin.grindfm.R;

/**
 * User: dsvoronin
 * Date: 04.04.12
 * Time: 21:36
 */
public class VKActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vkontakte);

        WebView vkontakteWebView = (WebView) findViewById(R.id.vkontakte_web_view);
        vkontakteWebView.loadUrl(getString(R.string.vkontakte_url));
        vkontakteWebView.requestFocus(View.FOCUS_DOWN);
        vkontakteWebView.setWebViewClient(new VkontakteWebViewClient());
    }


    private class VkontakteWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
