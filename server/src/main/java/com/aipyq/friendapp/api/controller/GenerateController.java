package com.aipyq.friendapp.api.controller;

import com.aipyq.friendapp.api.dto.*;
import com.aipyq.friendapp.service.GenerateService;
import com.aipyq.friendapp.service.QuotaService;
import com.aipyq.friendapp.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/generate")
public class GenerateController {

    private final GenerateService generateService;
    private final QuotaService quotaService;
    private final AnalyticsService analytics;
    public GenerateController(GenerateService generateService, QuotaService quotaService, AnalyticsService analytics) {
        this.generateService = generateService;
        this.quotaService = quotaService;
        this.analytics = analytics;
    }

    public static class CaptionReq { public String imageUrl; public Boolean ocr = true; }

    @PostMapping("/caption")
    public ResponseEntity<ImageInsights> caption(@RequestBody CaptionReq req) {
        ImageInsights insights = new ImageInsights();
        insights.setLabels(Arrays.asList("coffee", "latte", "morning"));
        insights.setOcrText(req.ocr != null && req.ocr ? "示例OCR文本" : null);
        return ResponseEntity.ok(insights);
    }

    @PostMapping("/copy")
    public ResponseEntity<CopyCandidates> copy(@RequestBody CopyRequest request, HttpServletRequest http) {
        String client = clientKey(http);
        boolean ok = quotaService.consume(client, QuotaService.Action.GENERATE);
        if (!ok) {
            return ResponseEntity.status(402).build();
        }
        CopyCandidates result = generateService.generate(request);
        analytics.track(client, "copy.generated", java.util.Map.of("items", result.getItems() != null ? result.getItems().size() : 0));
        return ResponseEntity.ok(result);
    }

    public static class RewriteReq { public String text; public String instruction; }

    @PostMapping("/rewrite")
    public ResponseEntity<RewriteReq> rewrite(@RequestBody RewriteReq req, HttpServletRequest http) {
        String client = clientKey(http);
        req.text = generateService.rewrite(req.text, req.instruction);
        analytics.track(client, "copy.rewritten", java.util.Map.of());
        return ResponseEntity.ok(req);
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
