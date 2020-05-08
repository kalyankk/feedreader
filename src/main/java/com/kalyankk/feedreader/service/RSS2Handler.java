package com.kalyankk.feedreader.service;

import com.kalyankk.feedreader.util.FeedItem;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

public class RSS2Handler extends DefaultHandler {
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
