package com.kalyankk.feedreader.util;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

public class FeedConfiguration {

    private String feedUrl;
    private FeedType feedType;
    private long intervalSeconds;
    private String tablePrefixName;

    private Boolean titleEnabled;
    private Boolean descriptionEnabled;
    private Boolean linkEnabled;
    private Boolean authorEnabled;
    private Boolean categoryEnabled;
    private Boolean guidEnabled;
    private Boolean pubDateEnabled;
    private Boolean sourceEnabled;

    private FeedConfiguration(String feedUrl, FeedType feedType, long intervalSeconds, String tablePrefixName,
                              Boolean titleEnabled, Boolean descriptionEnabled, Boolean linkEnabled,
                              Boolean authorEnabled, Boolean categoryEnabled, Boolean guidEnabled,
                              Boolean pubDateEnabled, Boolean sourceEnabled) {
        this.feedUrl = feedUrl;
        this.feedType = feedType;
        this.intervalSeconds = intervalSeconds;
        this.tablePrefixName = tablePrefixName;

        this.titleEnabled = titleEnabled;
        this.descriptionEnabled = descriptionEnabled;
        this.linkEnabled = linkEnabled;
        this.authorEnabled = authorEnabled;
        this.categoryEnabled = categoryEnabled;
        this.guidEnabled = guidEnabled;
        this.pubDateEnabled = pubDateEnabled;
        this.sourceEnabled = sourceEnabled;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public Boolean getTitleEnabled() {
        return titleEnabled;
    }

    public Boolean getDescriptionEnabled() {
        return descriptionEnabled;
    }

    public Boolean getLinkEnabled() {
        return linkEnabled;
    }

    public Boolean getAuthorEnabled() {
        return authorEnabled;
    }

    public Boolean getCategoryEnabled() {
        return categoryEnabled;
    }

    public Boolean getGuidEnabled() {
        return guidEnabled;
    }

    public Boolean getPubDateEnabled() {
        return pubDateEnabled;
    }

    public Boolean getSourceEnabled() {
        return sourceEnabled;
    }

    public FeedType getFeedType() {
        return feedType;
    }

    public long getIntervalSeconds() {
        return intervalSeconds;
    }

    public String getTablePrefixName() {
        return tablePrefixName;
    }

    public static class Builder {

        private String feedUrl;
        private FeedType feedType = FeedType.RSS2;
        private long intervalSeconds = 600;
        private String tablePrefixName = "SAMPLE";

        private Boolean titleEnabled = Boolean.TRUE;
        private Boolean descriptionEnabled = Boolean.TRUE;
        private Boolean linkEnabled = Boolean.TRUE;
        private Boolean authorEnabled = Boolean.FALSE;
        private Boolean categoryEnabled = Boolean.FALSE;
        private Boolean guidEnabled = Boolean.FALSE;
        private Boolean pubDateEnabled = Boolean.FALSE;
        private Boolean sourceEnabled = Boolean.FALSE;

        public Builder() { }

        public FeedConfiguration build() {
            return new FeedConfiguration(feedUrl, feedType, intervalSeconds, tablePrefixName,
                    titleEnabled, descriptionEnabled, linkEnabled,
                    authorEnabled, categoryEnabled, guidEnabled,
                    pubDateEnabled, sourceEnabled);
        }

        public Builder setFeedUrl(String url){
            this.feedUrl = url;
            return this;
        }

        public Builder enableTitle(Boolean enable){
            this.titleEnabled = enable;
            return this;
        }
        public Builder enableDescription(Boolean enable){
            this.descriptionEnabled = enable;
            return this;
        }
        public Builder enableLink(Boolean enable){
            this.linkEnabled = enable;
            return this;
        }
        public Builder enableAuthor(Boolean enable){
            this.authorEnabled = enable;
            return this;
        }
        public Builder enableCategory(Boolean enable){
            this.categoryEnabled = enable;
            return this;
        }
        public Builder enableGuid(Boolean enable){
            this.guidEnabled = enable;
            return this;
        }
        public Builder enablePublishedDate(Boolean enable){
            this.pubDateEnabled = enable;
            return this;
        }
        public Builder enableSource(Boolean enable){
            this.sourceEnabled = enable;
            return this;
        }
        public Builder setFeedType(FeedType feedType){
            this.feedType = feedType;
            return this;
        }
        public Builder setInterval(long interval, TimeUnit tu){
            this.intervalSeconds = tu.toSeconds(interval);
            return this;
        }
        public Builder setTablePrefixName(String tablePrefixName) {
            this.tablePrefixName = tablePrefixName;
            return this;
        }
    }

}
