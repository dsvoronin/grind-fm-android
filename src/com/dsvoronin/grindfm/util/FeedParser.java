package com.dsvoronin.grindfm.util;

import com.dsvoronin.grindfm.model.NewsItem;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

public interface FeedParser {
    List<NewsItem> parse() throws SAXException, ParserConfigurationException, IOException;
}
