package com.dsvoronin.grindfm;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import com.dsvoronin.grindfm.model.NewsItem;

public class NewsDialog extends Dialog {

    private static final String HTML_REGEXP = "<.*?>";


    private NewsItem message;

    public NewsDialog(Context context, NewsItem message) {
        super(context, R.style.ActivityDialog);
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_dialog);

        TextView title = (TextView) findViewById(R.id.dialogTitle);
        title.setText(message.getTitle());

        TextView description = (TextView) findViewById(R.id.description);
        description.setText(clearSpecialChars(clearHTML(message.getDescription())));
    }

    public static String clearHTML(String dirtyString) {
        return dirtyString.replaceAll(HTML_REGEXP, "").trim();
    }

    public static String clearSpecialChars(String str) {
        if (str.contains("&amp;")) {
            str = str.replace("&amp;", "&");
        } else if (str.contains("&lt;")) {
            str = str.replace("&lt;", "<");
        } else if (str.contains("&gt;")) {
            str = str.replace("&gt;", ">");
        } else if (str.contains("&apos;")) {
            str = str.replace("&apos;", "'");
        } else if (str.contains("&quot;")) {
            str = str.replace("&quot;", "\"");
        } else if (str.contains("&laquo;")) {
            str = str.replace("&laquo;", "\"");
        } else if (str.contains("&raquo;")) {
            str = str.replace("&raquo;", "\"");
        }
        return str;
    }

}
