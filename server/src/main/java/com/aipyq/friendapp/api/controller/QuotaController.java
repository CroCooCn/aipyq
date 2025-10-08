package com.aipyq.friendapp.api.controller;

import com.aipyq.friendapp.service.QuotaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/quota")
public class QuotaController {
    private final QuotaService quotaService;
    public QuotaController(QuotaService quotaService) { this.quotaService = quotaService; }

    @GetMapping
    public ResponseEntity<java.util.Map<String, Object>> get(HttpServletRequest http) {
        String key = clientKey(http);
        return ResponseEntity.ok(quotaService.snapshot(key));
    }

    private String clientKey(HttpServletRequest http) {
        String h = http.getHeader("X-Client-Id");
        if (h != null && !h.isBlank()) return h;
        String auth = http.getHeader("Authorization");
        if (auth != null && !auth.isBlank()) return auth;
        String ip = http.getRemoteAddr();
        return "ip:" + ip;
    }
}

