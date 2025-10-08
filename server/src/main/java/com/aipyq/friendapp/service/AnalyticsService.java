package com.aipyq.friendapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AnalyticsService {
    private static final Logger log = LoggerFactory.getLogger("analytics");

    public void track(String clientKey, String event, Map<String, Object> props) {
        log.info("event={} client={} props={}", event, clientKey, props);
    }
}

