package com.dsvoronin.grindfm.util;

import com.dsvoronin.grindfm.Message;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

public interface FeedParser {
    List<Message> parse() throws SAXException, ParserConfigurationException, IOException;
}
