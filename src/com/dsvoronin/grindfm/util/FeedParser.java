package com.dsvoronin.grindfm.util;

import com.dsvoronin.grindfm.Message;

import java.util.List;

public interface FeedParser {
    List<Message> parse();
}
