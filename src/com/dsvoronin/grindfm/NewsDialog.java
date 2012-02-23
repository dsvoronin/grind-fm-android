package com.dsvoronin.grindfm;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import com.dsvoronin.grindfm.model.NewsItem;
import com.dsvoronin.grindfm.util.StringUtil;

public class NewsDialog extends Dialog {

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
        description.setText(StringUtil.clearSpecialChars(StringUtil.clearHTML(message.getDescription())));
    }


}
