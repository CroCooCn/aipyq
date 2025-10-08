package com.aipyq.friendapp.api.dto;

import java.util.List;

public class Persona {
    private String role;
    private String tone;
    private List<String> brandKeywords;
    private List<String> bannedWords;
    private List<String> audienceTags;

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getTone() { return tone; }
    public void setTone(String tone) { this.tone = tone; }
    public List<String> getBrandKeywords() { return brandKeywords; }
    public void setBrandKeywords(List<String> brandKeywords) { this.brandKeywords = brandKeywords; }
    public List<String> getBannedWords() { return bannedWords; }
    public void setBannedWords(List<String> bannedWords) { this.bannedWords = bannedWords; }
    public List<String> getAudienceTags() { return audienceTags; }
    public void setAudienceTags(List<String> audienceTags) { this.audienceTags = audienceTags; }
}

