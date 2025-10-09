package com.aipyq.friendapp.api.controller;

import com.aipyq.friendapp.store.InMemoryStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/history")
public class HistoryController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(@RequestParam(name = "page", defaultValue = "1") int page,
                                                    @RequestParam(name = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(InMemoryStore.get().pageGenerations(page, size));
    }

    public static class SaveReq {
        public String copyText;
        public String imageUrl;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody SaveReq req) {
        if (req.copyText == null || req.copyText.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "copyText required"));
        }
        var g = InMemoryStore.get().addManualGeneration(req.copyText, req.imageUrl);
        return ResponseEntity.ok(Map.of("ok", true, "id", g.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String id) {
        boolean ok = InMemoryStore.get().deleteGeneration(id);
        return ResponseEntity.ok(Map.of("ok", ok));
    }
}
