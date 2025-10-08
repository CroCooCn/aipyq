package com.aipyq.friendapp.api.controller;

import com.aipyq.friendapp.store.InMemoryStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/history")
public class HistoryController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(InMemoryStore.get().pageGenerations(page, size));
    }
}
