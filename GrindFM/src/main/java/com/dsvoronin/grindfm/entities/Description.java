package com.dsvoronin.grindfm.entities;

import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.text.style.ImageSpan;

public class Description {

    private final String pureText;

    @Nullable
    private final String imageUrl;

    public Description(Article article) {
        Spanned spanned = Html.fromHtml(article.getDescription());

        pureText = spanned.toString();

        ImageSpan[] imageSpans = spanned.getSpans(0, spanned.length(), ImageSpan.class);
        if (imageSpans.length > 0) {
            imageUrl = imageSpans[0].getSource();
        } else {
            imageUrl = null;
        }
    }

    public String getPureText() {
        return pureText;
    }

    @Nullable
    public String getImageUrl() {
        return imageUrl;
    }
}
