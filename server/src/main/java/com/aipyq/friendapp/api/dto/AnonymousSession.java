package com.aipyq.friendapp.api.dto;

import java.time.OffsetDateTime;

public class AnonymousSession {
    private String token;
    private OffsetDateTime expiresAt;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }
}

