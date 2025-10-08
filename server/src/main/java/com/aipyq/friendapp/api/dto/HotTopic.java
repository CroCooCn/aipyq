package com.aipyq.friendapp.api.dto;

import java.util.List;

public class HotTopic {
    private String version;
    private String date;
    private List<String> keywords;

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }
}

