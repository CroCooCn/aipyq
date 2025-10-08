package com.aipyq.friendapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "quota")
public class QuotaProperties {
    private int visitorDailyCredits = 50; // 游客每日总额度
    private int monthlyCredits = 5000;    // 月订阅总额度（30天）
    private int costGenerate = 1;         // 生成文案消耗
    private int costRender = 1;           // 渲染消耗

    public int getVisitorDailyCredits() { return visitorDailyCredits; }
    public void setVisitorDailyCredits(int v) { this.visitorDailyCredits = v; }
    public int getMonthlyCredits() { return monthlyCredits; }
    public void setMonthlyCredits(int monthlyCredits) { this.monthlyCredits = monthlyCredits; }
    public int getCostGenerate() { return costGenerate; }
    public void setCostGenerate(int costGenerate) { this.costGenerate = costGenerate; }
    public int getCostRender() { return costRender; }
    public void setCostRender(int costRender) { this.costRender = costRender; }
}

