package com.kalyankk.feedreader.service;

import com.kalyankk.feedreader.config.FeedConfiguration;
import com.kalyankk.feedreader.config.FeedType;
import com.kalyankk.feedreader.util.FeedItem;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.ArrayList;
import java.util.List;

public class FeedReader {

    public static List<FeedItem> getFeedItems(String uri, FeedType feedType) throws Exception{
        if(feedType == FeedType.RSS2)
            return parseRSS2Feed(uri);
        else if(feedType == FeedType.JSON)
            return parseJSONFeed(uri);
        return new ArrayList<FeedItem>();
    }

    public static List<FeedItem> parseRSS2Feed(String uri) throws Exception{
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        RSS2Handler rss2Handler = new RSS2Handler();
        saxParser.parse(uri, rss2Handler);
        return rss2Handler.getFeedItemList();
    }

    public static List<FeedItem> parseJSONFeed(String uri){
        return new ArrayList<FeedItem>();
    }


    }
