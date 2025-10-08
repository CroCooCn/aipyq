package com.aipyq.friendapp.api.dto;

import java.time.OffsetDateTime;

public class Generation {
    private String id;
    private String imageId;
    private String selectedCopy;
    private OffsetDateTime createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getImageId() { return imageId; }
    public void setImageId(String imageId) { this.imageId = imageId; }
    public String getSelectedCopy() { return selectedCopy; }
    public void setSelectedCopy(String selectedCopy) { this.selectedCopy = selectedCopy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}

