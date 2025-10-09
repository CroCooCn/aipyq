package com.aipyq.friendapp.api.dto;

import java.util.List;

public class ImageInsights {
    private List<String> labels;
    private String ocrText;
    private String summary;

    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }
    public String getOcrText() { return ocrText; }
    public void setOcrText(String ocrText) { this.ocrText = ocrText; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}
