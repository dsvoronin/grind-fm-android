package com.dsvoronin.grindfm.network.rss;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "rss", strict = false)
public class RSS {

    @Element
    private Channel channel;

    @Attribute
    private String version;

    public Channel getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "RSS{" +
                "channel=" + channel +
                ", version='" + version + '\'' +
                '}';
    }
}