package com.kalyankk.feedreader;

import com.kalyankk.feedreader.service.DataCollectorService;
import com.kalyankk.feedreader.config.FeedConfiguration;
import com.kalyankk.feedreader.util.FeedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class Demo {

    public static void main(String args[]) throws Exception{

        /* Create table for master feed list

        create table master_feed_list (
                id int not null auto_increment primary key,
                feed_url varchar(255),
                feed_type varchar(255),
                interval_seconds long,
                table_prefix_name varchar(255),
                title_enabled boolean,
                description_enabled boolean,
                link_enabled boolean,
                author_enabled boolean,
                category_enabled boolean,
                guid_enabled boolean,
                pub_date_enabled boolean,
                source_enabled boolean
        )

        */


        FeedConfiguration rss2FeedConfig = getRSS2FeedConfiguration();
        DataCollectorService d1 = DataCollectorService.initializeService(rss2FeedConfig);
        d1.startService();

        //if(d1.isRunning()) d1.stopService();
        //if(!d1.isRunning()) d1.startService();

        FeedConfiguration jsonFeedConfig = getJSONFeedConfiguration();
        DataCollectorService d2 = DataCollectorService.initializeService(jsonFeedConfig);
        d2.startService();

        //if(d2.isRunning()) d2.stopService();
        //if(!d2.isRunning()) d2.startService();

    }

    static FeedConfiguration getRSS2FeedConfiguration() {
        return new FeedConfiguration.Builder()
                .setFeedUrl("https://finance.yahoo.com/news/rssindex")
                .enableAuthor(true)
                .enableCategory(true)
                .enableGuid(true)
                .setFeedType(FeedType.RSS2)
                .setInterval(1, TimeUnit.MINUTES)
                .setTablePrefixName("xml_feed")
                .build();
    }

    static FeedConfiguration getJSONFeedConfiguration() {
        return new FeedConfiguration.Builder()
                .setFeedUrl("https://daringfireball.net/feeds/json")
                .enableAuthor(true)
                .enableCategory(true)
                .enableGuid(true)
                .enableSource(true)
                .enableGuid(true)
                .setFeedType(FeedType.JSON)
                .setInterval(2, TimeUnit.MINUTES)
                .setTablePrefixName("json_feed")
                .build();
    }
}
