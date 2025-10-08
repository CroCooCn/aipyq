package com.aipyq.friendapp.api.dto;

import java.util.List;

public class RenderRequest {
    private String templateId;
    private String primaryColor;
    private String fontFamily;
    private Double fontSize;
    private Double lineHeight;
    private Double margin;
    private Boolean watermarkOn;
    private List<String> stickerSet;
    private String ratio;
    private String resolution;
    private Grid grid;
    private String imageUrl;
    private String copyText;

    public static class Grid {
        private Boolean enabled;
        private String size;
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
        public String getSize() { return size; }
        public void setSize(String size) { this.size = size; }
    }

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    public String getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }
    public String getFontFamily() { return fontFamily; }
    public void setFontFamily(String fontFamily) { this.fontFamily = fontFamily; }
    public Double getFontSize() { return fontSize; }
    public void setFontSize(Double fontSize) { this.fontSize = fontSize; }
    public Double getLineHeight() { return lineHeight; }
    public void setLineHeight(Double lineHeight) { this.lineHeight = lineHeight; }
    public Double getMargin() { return margin; }
    public void setMargin(Double margin) { this.margin = margin; }
    public Boolean getWatermarkOn() { return watermarkOn; }
    public void setWatermarkOn(Boolean watermarkOn) { this.watermarkOn = watermarkOn; }
    public List<String> getStickerSet() { return stickerSet; }
    public void setStickerSet(List<String> stickerSet) { this.stickerSet = stickerSet; }
    public String getRatio() { return ratio; }
    public void setRatio(String ratio) { this.ratio = ratio; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public Grid getGrid() { return grid; }
    public void setGrid(Grid grid) { this.grid = grid; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getCopyText() { return copyText; }
    public void setCopyText(String copyText) { this.copyText = copyText; }
}

