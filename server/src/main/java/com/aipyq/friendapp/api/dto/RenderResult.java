package com.aipyq.friendapp.api.dto;

import java.util.List;

public class RenderResult {
    private List<String> images;
    private String generationId;
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public String getGenerationId() { return generationId; }
    public void setGenerationId(String generationId) { this.generationId = generationId; }
}
