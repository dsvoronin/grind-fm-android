package com.dsvoronin.grindfm.entities;

import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.text.style.ImageSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Description {

    private static final String YOUTUBE_IMAGE_TEMPLATE = "http://i1.ytimg.com/vi/%s/maxresdefault.jpg";

    private final String pureText;

    @Nullable
    private final String imageUrl;

    public Description(Article article) {
        String rawDescription = article.getDescription();
        Spanned spanned = Html.fromHtml(rawDescription);

        pureText = spanned.toString();

        ImageSpan[] imageSpans = spanned.getSpans(0, spanned.length(), ImageSpan.class);
        if (imageSpans.length > 0) {
            imageUrl = imageSpans[0].getSource();
        } else {
            Pattern pattern = Pattern.compile("/(https?:\\/\\/)?(www.)?(youtube\\.com|youtu\\.be|youtube-nocookie\\.com)\\/(?:embed\\/|v\\/|watch\\?v=|watch\\?list=(.*)&v=)?((\\w|-){11})(&list=(\\w+)&?)?");
            Matcher matcher = pattern.matcher(rawDescription);
            if (matcher.find()) {
                String youtubeImage;
                try {
                    youtubeImage = String.format(YOUTUBE_IMAGE_TEMPLATE, matcher.group(5));
                } catch (IllegalStateException e) {
                    youtubeImage = null;
                }
                imageUrl = youtubeImage;
            } else {
                imageUrl = null;
            }
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
