package com.kalyankk.feedreader.util;

import com.kalyankk.feedreader.Demo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvalidFeedConfigurationException extends Exception{

    private static final Logger logger = LoggerFactory.getLogger(InvalidFeedConfigurationException.class);

    public InvalidFeedConfigurationException(String msg){
        super(msg);
        logger.error("Exception: InvalidFeedConfigurationException");
    }
}
