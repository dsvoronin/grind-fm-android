package com.dsvoronin.grindfm.util;

import com.dsvoronin.grindfm.Message;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

import static com.dsvoronin.grindfm.util.BaseFeedParser.*;

public class RssHandler extends DefaultHandler {

    private List<Message> messages;
    private Message currentMessage;
    private StringBuilder builder;

    public List<Message> getMessages() {
        return this.messages;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        messages = new ArrayList<Message>();
        builder = new StringBuilder();
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, name, attributes);
        if (localName.equalsIgnoreCase(ITEM)) {
            this.currentMessage = new Message();
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
                currentMessage.setTitle(builder.toString());
            } else if (localName.equalsIgnoreCase(LINK)) {
                currentMessage.setLink(builder.toString());
            } else if (localName.equalsIgnoreCase(DESCRIPTION)) {
                currentMessage.setDescription(builder.toString());
            } else if (localName.equalsIgnoreCase(PUB_DATE)) {
                currentMessage.setDate(builder.toString());
            } else if (localName.equalsIgnoreCase(ITEM)) {
                messages.add(currentMessage);
            }
            builder.setLength(0);
        }
    }
}