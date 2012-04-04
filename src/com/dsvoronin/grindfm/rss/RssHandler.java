package com.dsvoronin.grindfm.rss;

import com.dsvoronin.grindfm.model.NewsItem;
import org.jsoup.Jsoup;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

import static com.dsvoronin.grindfm.rss.BaseFeedParser.*;

public class RssHandler extends DefaultHandler {

    private List<NewsItem> messages;
    private NewsItem currentMessage;
    private StringBuilder builder;

    public List<NewsItem> getMessages() {
        return this.messages;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        messages = new ArrayList<NewsItem>();
        builder = new StringBuilder();
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, name, attributes);
        if (localName.equalsIgnoreCase(ITEM)) {
            this.currentMessage = new NewsItem();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        if (currentMessage != null) {
            builder.append(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        super.endElement(uri, localName, name);
        if (this.currentMessage != null) {
            if (localName.equalsIgnoreCase(TITLE)) {
                currentMessage.setTitle(Jsoup.parse(builder.toString()).text());
            } else if (localName.equalsIgnoreCase(LINK)) {
                currentMessage.setLink(builder.toString());
            } else if (localName.equalsIgnoreCase(DESCRIPTION)) {
                String desc = builder.toString();
                currentMessage.setDescription(desc);
                try {
                    currentMessage.setThumbImage(Jsoup.parse(desc).select("img").first().absUrl("src"));
                } catch (NullPointerException pne) {
                    currentMessage.setThumbImage(null);
                }
                currentMessage.setTextDescription(Jsoup.parse(desc).text().replace("(Читать далее...)", ""));
                try {
                    currentMessage.setVideo(Jsoup.parse(desc).select("embed").first().absUrl("src"));
                } catch (NullPointerException npe) {
                    currentMessage.setVideo(null);
                }
            } else if (localName.equalsIgnoreCase(PUB_DATE)) {
                currentMessage.setDate(builder.toString());
            } else if (localName.equalsIgnoreCase(ITEM)) {
                messages.add(currentMessage);
            }
            builder.setLength(0);
        }
    }
}