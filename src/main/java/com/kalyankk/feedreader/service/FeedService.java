package com.kalyankk.feedreader.service;

import com.kalyankk.feedreader.InvalidFeedConfigurationException;
import com.kalyankk.feedreader.util.FeedConfiguration;
import com.kalyankk.feedreader.util.FeedType;

import java.sql.*;

public class FeedService {

    public static void startService(FeedConfiguration config) throws Exception{

        if(config.getFeedUrl() == null)
            throw new InvalidFeedConfigurationException("Feed URL cannot be null");

        if(config.getFeedType() == null)
            throw new InvalidFeedConfigurationException("Feed Type cannot be null");

        if(config.getIntervalSeconds() < 60)
            throw new InvalidFeedConfigurationException("Feed interval should not be less than 1 minute");

        if(config.getFeedType() == FeedType.RSS1 || config.getFeedType() == FeedType.ATOM)
            throw new InvalidFeedConfigurationException("RSS1 and ATOM based RSS feed reading service is not yet implemented. Try RSS2 or JSON based RSS Feed");

        if(!(config.getTitleEnabled() || config.getAuthorEnabled() || config.getCategoryEnabled() || config.getDescriptionEnabled()
            || config.getGuidEnabled() || config.getLinkEnabled() || config.getPubDateEnabled() || config.getSourceEnabled()))
            throw new InvalidFeedConfigurationException("At least one column has to be enabled");


        int lastInsertedId = prepareDatabaseTable(config);
        String tableName = config.getTablePrefixName()+"_items_"+lastInsertedId;


        //then start timer task to get feed from url and save it to database

    }

    private static int prepareDatabaseTable(FeedConfiguration config) throws SQLException {

        Connection dbConn = DataSource.getConnection();

        String sql = "INSERT INTO master_feed_list "+
                    "( feed_url, feed_type, interval_seconds, table_prefix_name, title_enabled, description_enabled, link_enabled, author_enabled, category_enabled, guid_enabled, pub_date_enabled, source_enabled)"+
                    "values (?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement stmt = dbConn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, config.getFeedUrl());
        stmt.setString(2, config.getFeedType().toString());
        stmt.setLong(3, config.getIntervalSeconds());
        stmt.setString(4, config.getTablePrefixName());

        stmt.setBoolean(5, config.getTitleEnabled());
        stmt.setBoolean(6, config.getDescriptionEnabled());
        stmt.setBoolean(7, config.getLinkEnabled());
        stmt.setBoolean(8, config.getAuthorEnabled());
        stmt.setBoolean(9, config.getCategoryEnabled());
        stmt.setBoolean(10, config.getGuidEnabled());
        stmt.setBoolean(11, config.getPubDateEnabled());
        stmt.setBoolean(12, config.getSourceEnabled());

        stmt.executeUpdate();

        int last_inserted_id = 0;

        ResultSet rs = stmt.getGeneratedKeys();
        if(rs.next())
            last_inserted_id = rs.getInt(1);
        else throw new SQLException("Unable to prepare database table record on MASTER_FEED_LIST list");


        //create table
        String createTableSql = "CREATE TABLE "+ config.getTablePrefixName()+ "_items_" +last_inserted_id+
                "(id INTEGER not NULL AUTO_INCREMENT, " +
                (config.getTitleEnabled()?" title VARCHAR(255), ":"") +
                (config.getDescriptionEnabled()?" description VARCHAR(255), ":"") +
                (config.getLinkEnabled()?" link VARCHAR(255), ":"") +
                (config.getAuthorEnabled()?" author VARCHAR(255), ":"") +
                (config.getCategoryEnabled()?" category VARCHAR(255), ":"") +
                (config.getGuidEnabled()?" guid VARCHAR(255), ":"") +
                (config.getPubDateEnabled()?" pub_date VARCHAR(255), ":"") +
                (config.getSourceEnabled()?" source VARCHAR(255), ":"") +
                " PRIMARY KEY ( id ))";

        Statement createTableStmt = dbConn.createStatement();
        createTableStmt.executeUpdate(createTableSql);

        return last_inserted_id;

    }

}
