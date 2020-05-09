package com.kalyankk.feedreader.service;

import com.kalyankk.feedreader.Demo;
import com.kalyankk.feedreader.config.DataSource;
import com.kalyankk.feedreader.config.FeedConfiguration;
import com.kalyankk.feedreader.util.FeedItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FeedWriter {

    private static final Logger logger = LoggerFactory.getLogger(FeedWriter.class);

    public static void saveFeedItem(FeedItem feedItem, FeedConfiguration config, String tableName) throws SQLException {
        //save feed item to dtabase

        if(config.getGuidEnabled() || config.getLinkEnabled()) {
            //check if guid / link already exists
            //if already exists, skip
            Connection c = DataSource.getConnection();
            PreparedStatement s ;
            String logMsg;
            if(!config.getLinkEnabled()) {
                logMsg = "Feed item with guid : " + feedItem.getGuid() +" exists on table " + tableName;
                s = c.prepareStatement("select id from "+tableName+" where guid like ?");
                s.setString(1, feedItem.getGuid());
            }
            else if(!config.getGuidEnabled()) {
                logMsg = "Feed item with link : " + feedItem.getLink() +" exists on table " + tableName;
                s = c.prepareStatement("select id from "+tableName+" where link like ?");
                s.setString(1, feedItem.getLink());
            }
            else {
                logMsg = "Fedd item with guid : " + feedItem.getGuid() + " url : "+ feedItem +" exists on table " + tableName;
                s = c.prepareStatement("select id from "+tableName+" where guid like ? or link like ?");
                s.setString(1, feedItem.getGuid());
                s.setString(2, feedItem.getLink());
            }

            if(s.executeQuery().next())
            {
                //logger.info(logMsg, Boolean.TRUE);
                //logger.warn("duplicate record, skip insert to database table");
                c.close();
                return;
            }
            c.close();
        }

        StringBuilder sb1 = new StringBuilder("INSERT into "+tableName+" (");
        StringBuilder sb2 = new StringBuilder(" values (");
        if(config.getTitleEnabled()) {
            sb1.append("title,");
            sb2.append("?,");
        }
        if(config.getDescriptionEnabled()) {
            sb1.append("description,");
            sb2.append("?,");
        }
        if(config.getLinkEnabled()) {
            sb1.append("link,");
            sb2.append("?,");
        }
        if(config.getAuthorEnabled()) {
            sb1.append("author,");
            sb2.append("?,");
        }
        if(config.getCategoryEnabled()) {
            sb1.append("category,");
            sb2.append("?,");
        }
        if(config.getGuidEnabled()) {
            sb1.append("guid,");
            sb2.append("?,");
        }
        if(config.getPubDateEnabled()) {
            sb1.append("pub_date,");
            sb2.append("?,");
        }
        if(config.getSourceEnabled()) {
            sb1.append("source,");
            sb2.append("?,");
        }
        logger.info("Executing " + sb1.substring(0, sb1.length()-1)+")"+sb2.substring(0, sb2.length()-1)+")");
        Connection conn = DataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sb1.substring(0, sb1.length()-1)+")"+sb2.substring(0, sb2.length()-1)+")");
        int i = 1;
        if(config.getTitleEnabled())
            stmt.setString(i++, feedItem.getTitle());

        if(config.getDescriptionEnabled())
            stmt.setString(i++, feedItem.getDescription());

        if(config.getLinkEnabled())
            stmt.setString(i++, feedItem.getLink());

        if(config.getAuthorEnabled())
            stmt.setString(i++, feedItem.getAuthor());

        if(config.getCategoryEnabled())
            stmt.setString(i++, feedItem.getCategory());

        if(config.getGuidEnabled())
            stmt.setString(i++, feedItem.getGuid());

        if(config.getPubDateEnabled())
            stmt.setString(i++, feedItem.getPubDate());

        if(config.getSourceEnabled())
            stmt.setString(i++, feedItem.getSource());

        stmt.executeUpdate();
        conn.close();


    }

}
