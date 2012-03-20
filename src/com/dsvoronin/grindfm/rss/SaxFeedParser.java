package com.dsvoronin.grindfm.rss;

import com.dsvoronin.grindfm.model.NewsItem;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

public class SaxFeedParser extends BaseFeedParser {

    public SaxFeedParser(String feedUrl) throws MalformedURLException {
        super(feedUrl);
    }

    public List<NewsItem> parse() throws SAXException, ParserConfigurationException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        RssHandler handler = new RssHandler();
        parser.parse(this.getInputStream(), handler);
        return handler.getMessages();
    }
}