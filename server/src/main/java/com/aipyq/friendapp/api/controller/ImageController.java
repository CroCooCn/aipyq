package com.aipyq.friendapp.api.controller;

import com.aipyq.friendapp.api.dto.RenderRequest;
import com.aipyq.friendapp.api.dto.RenderResult;
import com.aipyq.friendapp.api.dto.Generation;
import com.aipyq.friendapp.store.InMemoryStore;
import com.aipyq.friendapp.service.ImageRenderService;
import com.aipyq.friendapp.service.QuotaService;
import com.aipyq.friendapp.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;

@RestController
@RequestMapping("/image")
public class ImageController {

    private final ImageRenderService imageRenderService;
    private final QuotaService quotaService;
    private final AnalyticsService analytics;
    public ImageController(ImageRenderService imageRenderService, QuotaService quotaService, AnalyticsService analytics) {
        this.imageRenderService = imageRenderService;
        this.quotaService = quotaService;
        this.analytics = analytics;
    }

    @PostMapping("/render")
    public ResponseEntity<RenderResult> render(@RequestBody RenderRequest request, HttpServletRequest http) {
        String client = clientKey(http);
        boolean ok = quotaService.consume(client, QuotaService.Action.RENDER);
        if (!ok) {
            return ResponseEntity.status(402).build();
        }
        // Minimal rendering to PNG data URL
        RenderResult r = imageRenderService.render(request);

        Generation g = new Generation();
        g.setId(UUID.randomUUID().toString());
        g.setImageId(request.getImageUrl() != null ? request.getImageUrl() : "img-demo");
        g.setSelectedCopy(request.getCopyText());
        g.setCreatedAt(OffsetDateTime.now());
        InMemoryStore.get().addGeneration(g);
        r.setGenerationId(g.getId());

        analytics.track(client, "image.rendered", java.util.Map.of("count", r.getImages() != null ? r.getImages().size() : 0));
        return ResponseEntity.ok(r);
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
