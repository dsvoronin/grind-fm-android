package fm.grind.android.sync;

import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.text.style.ImageSpan;

import fm.grind.android.entities.dto.Article;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mapper {

    private static final Pattern EMBEDDED_YOUTUBE_PATTERN = Pattern.compile("/(https?://)?(www.)?(youtube\\.com|youtu\\.be|youtube-nocookie\\.com)/(?:embed/|v/|watch\\?v=|watch\\?list=(.*)&v=)?((\\w|-){11})(&list=(\\w+)&?)?");
    private static final String YOUTUBE_IMAGE_TEMPLATE = "http://i1.ytimg.com/vi/%s/maxresdefault.jpg";

    private SimpleDateFormat dbDateFormat;

    private SimpleDateFormat uiDateFormat;

    private Integer id;

    private String pureText;

    @Nullable
    private String imageUrl;

    private long pubDate = 0;

    private String formattedDate;

    private Article article;
    private Spanned spanned;

    public Mapper(Locale locale, Article article) {
        this.article = article;
        spanned = Html.fromHtml(article.getDescription());

        dbDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", locale);
        uiDateFormat = new SimpleDateFormat("EEE, d MMM HH:mm", locale);
    }

    public int getId() {
        if (id == null) {
            id = extractId(article);
        }
        return id;
    }

    public String getPureText() {
        if (pureText == null) {
            pureText = spanned.toString();
        }
        return pureText;
    }

    @Nullable
    public String getImageUrl() {
        if (imageUrl == null) {
            ImageSpan[] imageSpans = spanned.getSpans(0, spanned.length(), ImageSpan.class);
            if (imageSpans.length > 0) {
                imageUrl = imageSpans[0].getSource();
            } else {
                imageUrl = embeddedYoutube();
            }
        }
        return imageUrl;
    }

    public long getPubDate() {
        if (pubDate == 0) {
            try {
                pubDate = dbDateFormat.parse(article.getPubDate()).getTime();
            } catch (ParseException e) {
                pubDate = 0;
            }
        }
        return pubDate;
    }

    public String getFormattedDate() {
        if (formattedDate == null) {
            long pubDate = getPubDate();
            if (pubDate > 0) {
                formattedDate = uiDateFormat.format(pubDate);
            } else {
                formattedDate = "";
            }
        }
        return formattedDate;
    }

    /**
     * guid is a weird string like: "1653 at http://www.grind.fm"
     *
     * @return we need 1653 here
     */
    private int extractId(Article article) throws NumberFormatException {
        String guid = article.getGuid();
        return Integer.parseInt(guid.substring(0, guid.indexOf(' ')));
    }

    @Nullable
    private String embeddedYoutube() {
        Matcher matcher = EMBEDDED_YOUTUBE_PATTERN.matcher(article.getDescription());
        if (matcher.find()) {
            String youtubeImage;
            try {
                youtubeImage = String.format(YOUTUBE_IMAGE_TEMPLATE, matcher.group(5));
            } catch (IllegalStateException e) {
                youtubeImage = null;
            }
            return youtubeImage;
        } else {
            return null;
        }
    }
}
