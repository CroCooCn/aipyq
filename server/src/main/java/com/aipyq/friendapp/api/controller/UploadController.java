package com.aipyq.friendapp.api.controller;

import com.aipyq.friendapp.service.OssService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/upload")
public class UploadController {
    private final OssService ossService;

    public UploadController(OssService ossService) { this.ossService = ossService; }

    @PostMapping("/image")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "file is empty"));
        }
        String url = ossService.upload(file.getInputStream(), file.getSize(), file.getOriginalFilename(), file.getContentType());
        return ResponseEntity.ok(Map.of("ok", true, "url", url));
    }
}
