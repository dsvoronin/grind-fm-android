package com.dsvoronin.grindfm.util;

import com.dsvoronin.grindfm.Message;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.List;

public class SaxFeedParser extends BaseFeedParser {

    public SaxFeedParser(String feedUrl) {
        super(feedUrl);
    }

    public List<Message> parse() {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            RssHandler handler = new RssHandler();
            parser.parse(this.getInputStream(), handler);
            return handler.getMessages();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}