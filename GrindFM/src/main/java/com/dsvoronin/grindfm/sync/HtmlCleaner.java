package com.dsvoronin.grindfm.sync;

import com.dsvoronin.grindfm.entities.Article;
import com.dsvoronin.grindfm.entities.Description;

public class HtmlCleaner {

    /**
     * guid is a weird string like: "1653 at http://www.grind.fm"
     *
     * @return we need 1653 here
     */
    public static int extractId(Article article) throws NumberFormatException {
        String guid = article.getGuid();
        return Integer.parseInt(guid.substring(0, guid.indexOf(' ')));
    }

    public static Description cleanDescription(Article article) {
        return new Description(article);
    }
}
