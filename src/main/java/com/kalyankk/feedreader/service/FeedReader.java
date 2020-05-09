package com.kalyankk.feedreader.service;

import com.kalyankk.feedreader.config.FeedType;
import com.kalyankk.feedreader.util.FeedItem;
import com.kalyankk.feedreader.util.InvalidFeedConfigurationException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
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

    public static List<FeedItem> parseJSONFeed(String location) throws Exception{
        JSONObject obj ;
        if(location.startsWith("http://") || location.startsWith("https://"))
        {
            try {
                URL url = new URL(location);
                URLConnection yc = url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        yc.getInputStream()));
                obj = (JSONObject) new JSONParser().parse(in);
                in.close();
            }catch(Exception e) {
                throw new InvalidFeedConfigurationException("Invalid URI");
            }
        }
        else {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        new FileInputStream(location)));
                obj = (JSONObject) new JSONParser().parse(in);
                in.close();
            }catch(Exception e) {
                throw new InvalidFeedConfigurationException("Invalid URI");
            }
        }

        List<FeedItem> res = new ArrayList<FeedItem>();

        if(!obj.containsKey("items"))
            throw new InvalidFeedConfigurationException("Invalid JSON Feed");
        JSONArray items = (JSONArray) obj.get("items");
        Iterator itr = items.iterator();
        while(itr.hasNext()){
            JSONObject jsonItemObject = (JSONObject) itr.next();
            FeedItem i = new FeedItem();

            if(jsonItemObject.containsKey("title"))
                i.setTitle(jsonItemObject.get("title").toString());

            if(jsonItemObject.containsKey("author"))
                i.setTitle(((JSONObject)jsonItemObject.get("author")).get("name").toString());

            if(jsonItemObject.containsKey("content_text"))
                i.setDescription(jsonItemObject.get("content_text").toString());
            else if(jsonItemObject.containsKey("content_html"))
                i.setDescription(jsonItemObject.get("content_html").toString());

            if(jsonItemObject.containsKey("url"))
                i.setLink(jsonItemObject.get("url").toString());

            if(jsonItemObject.containsKey("tags"))
                i.setCategory(jsonItemObject.get("tags").toString());

            if(jsonItemObject.containsKey("id"))
                i.setGuid(jsonItemObject.get("id").toString());

            if(jsonItemObject.containsKey("date_published"))
                i.setPubDate(jsonItemObject.get("date_published").toString());

            if(jsonItemObject.containsKey("external_url"))
                i.setSource(jsonItemObject.get("external_url").toString());

            res.add(i);
        }

        return res;
    }

    public static class RSS2Handler extends DefaultHandler {
        private static final String ITEM = "item";
        private static final String TITLE = "title";
        private static final String DESCRIPTION = "description";
        private static final String LINK = "link";
        private static final String AUTHOR = "author";
        private static final String CATEGORY = "category";
        private static final String GUID = "guid";
        private static final String PUB_DATE = "pubDate";
        private static final String SOURCE = "source";

        private List<FeedItem> feedItemList;
        private String elementValue;
        private FeedItem latestItem;

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            elementValue = new String(ch, start, length);
        }

        @Override
        public void startDocument() throws SAXException {
            feedItemList = new ArrayList<FeedItem>();
        }

        @Override
        public void startElement(String uri, String lName, String qName, Attributes attr) throws SAXException {
            if (ITEM.equalsIgnoreCase(qName)) {
                latestItem = new FeedItem();
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if(ITEM.equalsIgnoreCase(qName))
            {
                feedItemList.add(latestItem);
                latestItem = null;
            }

            if(latestItem == null)
                return;

            if (TITLE.equalsIgnoreCase(qName))
                latestItem.setTitle(elementValue);
            else if (AUTHOR.equalsIgnoreCase(qName))
                latestItem.setAuthor(elementValue);
            else if (DESCRIPTION.equalsIgnoreCase(qName))
                latestItem.setDescription(elementValue);
            else if (LINK.equalsIgnoreCase(qName))
                latestItem.setLink(elementValue);
            else if (CATEGORY.equalsIgnoreCase(qName))
                latestItem.setCategory(elementValue);
            else if (GUID.equalsIgnoreCase(qName))
                latestItem.setGuid(elementValue);
            else if (PUB_DATE.equalsIgnoreCase(qName))
                latestItem.setPubDate(elementValue);
            else if (SOURCE.equalsIgnoreCase(qName))
                latestItem.setSource(elementValue);
        }

        public List<FeedItem> getFeedItemList() {
            return feedItemList;
        }
    }


}
