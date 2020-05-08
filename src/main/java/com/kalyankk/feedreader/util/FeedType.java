package com.kalyankk.feedreader.util;

public enum FeedType {
    JSON("JSON"), // Refer: https://jsonfeed.org/mappingrssandatom
    RSS1("RSS1.0"),
    RSS2("RSS2.0"), // Refer: https://validator.w3.org/feed/docs/rss2.html
    ATOM("ATOM1.0");

    private String feedType;

    FeedType(String feedType) {
        feedType = feedType;
    }
}
