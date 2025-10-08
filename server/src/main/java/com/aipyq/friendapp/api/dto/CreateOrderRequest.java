package com.aipyq.friendapp.api.dto;

public class CreateOrderRequest {
    private String channel; // wechat, alipay
    private String planId;

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
}

