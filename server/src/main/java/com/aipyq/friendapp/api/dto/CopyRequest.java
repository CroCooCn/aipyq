package com.aipyq.friendapp.api.dto;

import java.util.List;

public class CopyRequest {
    private List<String> imageTags;
    private String ocrText;
    private String imageUrl;
    private Persona persona;
    private List<String> audienceTags;
    private Boolean hotTopicsOn = true;
    private String stylePreset;
    private String instruction;
    private Boolean reasoningOn;

    public List<String> getImageTags() { return imageTags; }
    public void setImageTags(List<String> imageTags) { this.imageTags = imageTags; }
    public String getOcrText() { return ocrText; }
    public void setOcrText(String ocrText) { this.ocrText = ocrText; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Persona getPersona() { return persona; }
    public void setPersona(Persona persona) { this.persona = persona; }
    public List<String> getAudienceTags() { return audienceTags; }
    public void setAudienceTags(List<String> audienceTags) { this.audienceTags = audienceTags; }
    public Boolean getHotTopicsOn() { return hotTopicsOn; }
    public void setHotTopicsOn(Boolean hotTopicsOn) { this.hotTopicsOn = hotTopicsOn; }
    public String getStylePreset() { return stylePreset; }
    public void setStylePreset(String stylePreset) { this.stylePreset = stylePreset; }
    public String getInstruction() { return instruction; }
    public void setInstruction(String instruction) { this.instruction = instruction; }
    public Boolean getReasoningOn() { return reasoningOn; }
    public void setReasoningOn(Boolean reasoningOn) { this.reasoningOn = reasoningOn; }
}
