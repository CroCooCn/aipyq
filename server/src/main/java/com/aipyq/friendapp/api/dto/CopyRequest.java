package com.aipyq.friendapp.api.dto;

public class CopyRequest {
    private String imageUrl;
    private String styleInstruction;
    private Boolean reasoningOn;
    private String roundId;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public String getRoundId() {
        return roundId;
    }

    public void setRoundId(String roundId) {
        this.roundId = roundId;
    }
}
