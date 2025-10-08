package com.aipyq.friendapp.api.controller;

import com.aipyq.friendapp.config.AiProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/debug")
public class DebugController {
    private final AiProperties props;
    public DebugController(AiProperties props) { this.props = props; }

    @GetMapping("/ai")
    public ResponseEntity<Map<String, Object>> ai() {
        return ResponseEntity.ok(Map.of(
                "provider", props.getProvider(),
                "baseUrl", props.getVolcBaseUrl(),
                "textEndpoint", props.getVolcTextEndpoint(),
                "visionEndpoint", props.getVolcVisionEndpoint(),
                "imageEditEndpoint", props.getVolcImageEditEndpoint(),
                "textModel", props.getVolcTextModel(),
                "visionModel", props.getVolcVisionModel(),
                "imageEditModel", props.getVolcImageEditModel()
        ));
    }
}

