package com.kalyankk.feedreader.service;

import com.kalyankk.feedreader.util.FeedItem;
import com.kalyankk.feedreader.util.InvalidFeedConfigurationException;
import com.kalyankk.feedreader.config.DataSource;
import com.kalyankk.feedreader.config.FeedConfiguration;
import com.kalyankk.feedreader.config.FeedType;

import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;

public class DataCollectorService{

    private FeedConfiguration config;
    private String tableName;
    private Timer timer;

    private DataCollectorService(FeedConfiguration config) {
        this.config = config;
        timer = new Timer();
    }

    public static DataCollectorService startService(FeedConfiguration config) throws Exception{

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


        DataCollectorService dataCollectorService = new DataCollectorService(config);
        dataCollectorService.prepareDatabaseTable();

        dataCollectorService.schedule();
        return dataCollectorService;
    }

    private void prepareDatabaseTable() throws SQLException {

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

        tableName = config.getTablePrefixName()+"_items_"+last_inserted_id;
    }

    private void schedule()
    {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //here get feed, update db
                System.out.println("Starting feed reader and writer");
                try {
                    for (FeedItem feedItem : FeedReader.getFeedItems(config.getFeedUrl(), config.getFeedType())) {
                        try {
                            FeedWriter.saveFeedItem(feedItem, config, tableName);
                        } catch (SQLException sqlException) {
                            System.err.println("Exception while saving feed to table :" + tableName + ":" + sqlException.getMessage());
                            sqlException.printStackTrace();
                        }
                    }
                }catch (Exception e){
                    System.out.println("FeedReader : Unable to process : " + config.getFeedUrl());
                    e.printStackTrace();
                }
            }
        }, 0, config.getIntervalSeconds() * 1000);
    }
}
