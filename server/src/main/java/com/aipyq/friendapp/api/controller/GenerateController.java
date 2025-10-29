package com.aipyq.friendapp.api.controller;

import com.aipyq.friendapp.api.dto.BatchCopyRequest;
import com.aipyq.friendapp.api.dto.CopyCandidates;
import com.aipyq.friendapp.api.dto.CopyRequest;
import com.aipyq.friendapp.api.dto.User;
import com.aipyq.friendapp.service.AuthService;
import com.aipyq.friendapp.service.CopyHistoryService;
import com.aipyq.friendapp.service.GenerateService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/generate")
public class GenerateController {

    private final GenerateService generateService;
    private final AuthService authService;

    public GenerateController(GenerateService generateService,
                              AuthService authService) {
        this.generateService = generateService;
        this.authService = authService;
    }

    @PostMapping("/copy")
    public ResponseEntity<CopyCandidates> copy(@RequestBody CopyRequest request, HttpServletRequest http) {
        if (request == null || !StringUtils.hasText(request.getImageUrl())) {
            return ResponseEntity.badRequest().build();
        }
        Optional<User> userOpt = authService.findUserByAuthorization(http.getHeader("Authorization"));
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        User user = userOpt.get();
        if (authService.deductQuota(user.getId(), 1).isEmpty()) {
            return ResponseEntity.status(402).build();
        }
        CopyHistoryService.HistoryIdentity identity = CopyHistoryService.HistoryIdentity.forUser(user.getId());
        CopyCandidates result = generateService.generate(identity, request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/copy/batch")
    public ResponseEntity<CopyCandidates> copyBatch(@RequestBody BatchCopyRequest request, HttpServletRequest http) {
        if (request == null || request.getImageUrls() == null) {
            return ResponseEntity.badRequest().build();
        }
        List<String> sanitized = request.getImageUrls().stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .toList();
        if (sanitized.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Optional<User> userOpt = authService.findUserByAuthorization(http.getHeader("Authorization"));
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        User user = userOpt.get();
        int cost = sanitized.size();
        if (authService.deductQuota(user.getId(), cost).isEmpty()) {
            return ResponseEntity.status(402).build();
        }
        BatchCopyRequest normalized = new BatchCopyRequest();
        normalized.setImageUrls(sanitized);
        normalized.setStyleInstruction(request.getStyleInstruction());
        normalized.setReasoningOn(request.getReasoningOn());
        CopyHistoryService.HistoryIdentity identity = CopyHistoryService.HistoryIdentity.forUser(user.getId());
        CopyCandidates result = generateService.generateBatch(identity, normalized);
        return ResponseEntity.ok(result);
    }
}
