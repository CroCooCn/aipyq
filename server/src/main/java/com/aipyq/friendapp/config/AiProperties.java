package com.aipyq.friendapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai")
public class AiProperties {
    private String provider = "mock";
    private String volcBaseUrl;
    private String volcApiKey;
    private String volcTextEndpoint;
    private String volcVisionEndpoint;
    private String volcImageEditEndpoint;
    private String volcTextModel;
    private String volcVisionModel;
    private String volcImageEditModel;

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getVolcBaseUrl() { return volcBaseUrl; }
    public void setVolcBaseUrl(String volcBaseUrl) { this.volcBaseUrl = volcBaseUrl; }
    public String getVolcApiKey() { return volcApiKey; }
    public void setVolcApiKey(String volcApiKey) { this.volcApiKey = volcApiKey; }
    public String getVolcTextEndpoint() { return volcTextEndpoint; }
    public void setVolcTextEndpoint(String volcTextEndpoint) { this.volcTextEndpoint = volcTextEndpoint; }
    public String getVolcVisionEndpoint() { return volcVisionEndpoint; }
    public void setVolcVisionEndpoint(String volcVisionEndpoint) { this.volcVisionEndpoint = volcVisionEndpoint; }
    public String getVolcImageEditEndpoint() { return volcImageEditEndpoint; }
    public void setVolcImageEditEndpoint(String volcImageEditEndpoint) { this.volcImageEditEndpoint = volcImageEditEndpoint; }
    public String getVolcTextModel() { return volcTextModel; }
    public void setVolcTextModel(String volcTextModel) { this.volcTextModel = volcTextModel; }
    public String getVolcVisionModel() { return volcVisionModel; }
    public void setVolcVisionModel(String volcVisionModel) { this.volcVisionModel = volcVisionModel; }
    public String getVolcImageEditModel() { return volcImageEditModel; }
    public void setVolcImageEditModel(String volcImageEditModel) { this.volcImageEditModel = volcImageEditModel; }
}

