package com.kalyankk.feedreader.service;

import com.kalyankk.feedreader.util.FeedItem;
import com.kalyankk.feedreader.util.InvalidFeedConfigurationException;
import com.kalyankk.feedreader.config.DataSource;
import com.kalyankk.feedreader.config.FeedConfiguration;
import com.kalyankk.feedreader.util.FeedType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;

public class DataCollectorService{

    private static final Logger logger = LoggerFactory.getLogger(FeedReader.class);

    private FeedConfiguration config;
    private String tableName;
    private Timer timer;
    private  Boolean running = Boolean.FALSE;

    private DataCollectorService(FeedConfiguration config) {
        logger.info("Preparing DataCollectorService for "+config.getTablePrefixName());
        this.config = config;
        timer = new Timer();
    }

    public static DataCollectorService initializeService(FeedConfiguration config) throws Exception{

        logger.info("Initializing DataCollectorService for "+ config.getTablePrefixName());
        if(config.getFeedUrl() == null) {
            logger.error("Feed URL cannot be null");
            throw new InvalidFeedConfigurationException("Feed URL cannot be null");
        }

        if(config.getFeedType() == null) {
            logger.error("Feed Type cannot be null");
            throw new InvalidFeedConfigurationException("Feed Type cannot be null");
        }

        if(config.getIntervalSeconds() < 60) {
            logger.error("Feed interval should not be less than 1 minute");
            throw new InvalidFeedConfigurationException("Feed interval should not be less than 1 minute");
        }

        if(config.getFeedType() == FeedType.RSS1 || config.getFeedType() == FeedType.ATOM) {
            logger.error("RSS1 and ATOM based RSS feed reading service is not yet implemented. Try RSS2 or JSON based RSS Feed");
            throw new InvalidFeedConfigurationException("RSS1 and ATOM based RSS feed reading service is not yet implemented. Try RSS2 or JSON based RSS Feed");
        }

        if(!(config.getTitleEnabled() || config.getAuthorEnabled() || config.getCategoryEnabled() || config.getDescriptionEnabled()
            || config.getGuidEnabled() || config.getLinkEnabled() || config.getPubDateEnabled() || config.getSourceEnabled())) {
            logger.error("At least one column has to be enabled");
            throw new InvalidFeedConfigurationException("At least one column has to be enabled");
        }


        DataCollectorService dataCollectorService = new DataCollectorService(config);
        dataCollectorService.prepareDatabaseTable();

        return dataCollectorService;
    }

    public void startService() {
        synchronized (this) {
            if (!running) {
                running = Boolean.TRUE;
                logger.info("Starting DataCollectorService for " + config.getTablePrefixName());
                this.schedule();
            } else {
                logger.error("DataCollectorService for " + config.getTablePrefixName() + " is already running");
            }
        }
    }

    public void stopService() {
        synchronized (this) {
            if (running) {
                running = Boolean.FALSE;
                logger.info("Stopping DataCollectorService for " + config.getTablePrefixName());
                timer.cancel();
            } else {
                logger.error("DataCollectorService for " + config.getTablePrefixName() + " is already stopped" );
            }
        }
    }

    public Boolean isRunning() {
        synchronized (this) {
            return running;
        }
    }
    private void prepareDatabaseTable() throws SQLException {

        logger.info("Adding entry to master list table for " + config.getTablePrefixName());

        Connection dbConn = DataSource.getConnection();

        String sql = "INSERT INTO master_feed_list "+
                    "( feed_url, feed_type, interval_seconds, table_prefix_name, title_enabled, description_enabled, link_enabled, author_enabled, category_enabled, guid_enabled, pub_date_enabled, source_enabled)"+
                    "values (?,?,?,?,?,?,?,?,?,?,?,?)";
        logger.warn("Executing : " + sql);
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
        if(rs.next()) {
            last_inserted_id = rs.getInt(1);
            logger.info("Entry created with master list table with unique id " + last_inserted_id + " for " + config.getTablePrefixName());
        }
        else{
            logger.error("Unable to create entry with master list table for " + config.getTablePrefixName());
            throw new SQLException("Unable to prepare database table record on MASTER_FEED_LIST list");
        }


        String createTableSql = "CREATE TABLE "+ config.getTablePrefixName()+ "_items_" +last_inserted_id+
                "(id INTEGER not NULL AUTO_INCREMENT, " +
                (config.getTitleEnabled()?" title VARCHAR(1000), ":"") +
                (config.getDescriptionEnabled()?" description TEXT, ":"") +
                (config.getLinkEnabled()?" link VARCHAR(500), ":"") +
                (config.getAuthorEnabled()?" author VARCHAR(200), ":"") +
                (config.getCategoryEnabled()?" category VARCHAR(200), ":"") +
                (config.getGuidEnabled()?" guid VARCHAR(200), ":"") +
                (config.getPubDateEnabled()?" pub_date VARCHAR(200), ":"") +
                (config.getSourceEnabled()?" source VARCHAR(500), ":"") +
                " PRIMARY KEY ( id ))";

        logger.warn("Executing : " + createTableSql);

        Statement createTableStmt = dbConn.createStatement();
        createTableStmt.executeUpdate(createTableSql);

        tableName = config.getTablePrefixName()+"_items_"+last_inserted_id;
        logger.info("Creating dynamic table "+tableName+" for " + config.getTablePrefixName());
    }

    private void schedule()
    {
        logger.info("Scheduling task for every "+config.getIntervalSeconds() + " sec for " + config.getTablePrefixName());
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //here get feed, update db
                logger.info("Running scheduled task for " + config.getTablePrefixName());
                try {
                    for (FeedItem feedItem : FeedReader.getFeedItems(config.getFeedUrl(), config.getFeedType())) {
                        try {
                            FeedWriter.saveFeedItem(feedItem, config, tableName);
                        } catch (SQLException sqlException) {
                            logger.error("Exception while saving feed item to table :" + tableName + ":" + sqlException.getMessage());
                            sqlException.printStackTrace();
                        }
                    }
                }catch (Exception e){
                    logger.error("FeedReader : Unable to process : " + config.getFeedUrl());
                    e.printStackTrace();
                }
            }
        }, 0, config.getIntervalSeconds() * 1000);
    }
}
