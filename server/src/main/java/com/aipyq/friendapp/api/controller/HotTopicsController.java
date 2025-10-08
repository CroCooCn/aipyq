package com.aipyq.friendapp.api.controller;

import com.aipyq.friendapp.api.dto.HotTopic;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;

@RestController
@RequestMapping("/hot-topics")
public class HotTopicsController {

    @GetMapping
    public ResponseEntity<HotTopic> get() {
        HotTopic h = new HotTopic();
        h.setVersion("2025.10.08");
        h.setDate(LocalDate.now().toString());
        h.setKeywords(Arrays.asList("城市烟火气", "开工第一杯", "秋日限定"));
        return ResponseEntity.ok(h);
    }
}

