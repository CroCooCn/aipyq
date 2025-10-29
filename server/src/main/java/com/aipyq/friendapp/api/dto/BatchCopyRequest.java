package com.aipyq.friendapp.api.dto;

import java.util.List;

public class BatchCopyRequest {
    private List<String> imageUrls;
    private String styleInstruction;
    private Boolean reasoningOn;

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getStyleInstruction() {
        return styleInstruction;
    }

    public void setStyleInstruction(String styleInstruction) {
        this.styleInstruction = styleInstruction;
    }

    public Boolean getReasoningOn() {
        return reasoningOn;
    }

    public void setReasoningOn(Boolean reasoningOn) {
        this.reasoningOn = reasoningOn;
    }
}
