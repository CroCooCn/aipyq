package com.aipyq.friendapp.api.dto;

public class User {
    private String id;
    private String phone;
    private String planId;
    private Integer remainingQuota;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    public Integer getRemainingQuota() { return remainingQuota; }
    public void setRemainingQuota(Integer remainingQuota) { this.remainingQuota = remainingQuota; }
}

