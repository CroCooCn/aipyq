package com.aipyq.friendapp.api.controller;

import com.aipyq.friendapp.api.dto.User;
import com.aipyq.friendapp.persistence.entity.CopyHistoryEntity;
import com.aipyq.friendapp.service.AuthService;
import com.aipyq.friendapp.service.CopyHistoryService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/history")
public class HistoryController {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final CopyHistoryService copyHistoryService;
    private final AuthService authService;

    public HistoryController(CopyHistoryService copyHistoryService, AuthService authService) {
        this.copyHistoryService = copyHistoryService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(@RequestParam(name = "page", defaultValue = "1") int page,
                                                    @RequestParam(name = "size", defaultValue = "20") int size,
                                                    HttpServletRequest http) {
        int pageNo = Math.max(page, 1);
        int pageSize = Math.max(1, Math.min(size, 100));
        Optional<User> userOpt = authService.findUserByAuthorization(http.getHeader("Authorization"));
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        CopyHistoryService.HistoryIdentity identity = CopyHistoryService.HistoryIdentity.forUser(userOpt.get().getId());
        IPage<CopyHistoryEntity> pageResult = copyHistoryService.pageHistory(identity, pageNo, pageSize);
        List<Map<String, Object>> grouped = groupByImage(pageResult.getRecords());
        return ResponseEntity.ok(Map.of(
                "items", grouped,
                "totalRounds", pageResult.getTotal(),
                "groupCount", grouped.size()
        ));
    }

    public static class SaveReq {
        public String copyText;
        public String imageUrl;
        public String imageId;
        public String instruction;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody SaveReq req, HttpServletRequest http) {
        if (req.copyText == null || req.copyText.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "copyText required"));
        }
        Optional<User> userOpt = authService.findUserByAuthorization(http.getHeader("Authorization"));
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        CopyHistoryService.HistoryIdentity identity = CopyHistoryService.HistoryIdentity.forUser(userOpt.get().getId());
        String imageId = resolveImageId(req.imageId, req.imageUrl, http);
        CopyHistoryEntity entity = copyHistoryService.appendHistory(
                identity,
                imageId,
                req.instruction,
                List.of(req.copyText),
                CopyHistoryService.Source.MANUAL
        );
        return ResponseEntity.ok(Map.of("ok", true, "id", entity.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String id, HttpServletRequest http) {
        Optional<User> userOpt = authService.findUserByAuthorization(http.getHeader("Authorization"));
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        CopyHistoryService.HistoryIdentity identity = CopyHistoryService.HistoryIdentity.forUser(userOpt.get().getId());
        Long numericId;
        try {
            numericId = Long.valueOf(id);
        } catch (NumberFormatException ex) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "invalid id"));
        }
        boolean ok = copyHistoryService.delete(identity, numericId);
        return ResponseEntity.ok(Map.of("ok", ok));
    }

    private String resolveImageId(String imageId, String imageUrl, HttpServletRequest http) {
        if (StringUtils.hasText(imageId)) {
            return imageId.trim();
        }
        if (StringUtils.hasText(imageUrl)) {
            return imageUrl.trim();
        }
        String fallback = http.getHeader("X-Client-Id");
        if (!StringUtils.hasText(fallback)) {
            fallback = http.getRemoteAddr();
        }
        return "session-" + fallback;
    }

    private List<Map<String, Object>> groupByImage(List<CopyHistoryEntity> records) {
        LinkedHashMap<String, Group> grouped = new LinkedHashMap<>();
        for (CopyHistoryEntity entity : records) {
            String imageId = StringUtils.hasText(entity.getImageId()) ? entity.getImageId() : "unknown";
            Group group = grouped.computeIfAbsent(imageId, Group::new);
            group.addRound(entity, copyHistoryService.extractOutputs(entity));
        }
        return grouped.values().stream()
                .sorted((g1, g2) -> {
                    LocalDateTime t1 = g1.latestAt;
                    LocalDateTime t2 = g2.latestAt;
                    if (Objects.equals(t1, t2)) {
                        return 0;
                    }
                    if (t1 == null) {
                        return 1;
                    }
                    if (t2 == null) {
                        return -1;
                    }
                    return t2.compareTo(t1);
                })
                .map(Group::toMap)
                .toList();
    }

    private static class Group {
        private final String imageId;
        private final List<Map<String, Object>> rounds = new ArrayList<>();
        private LocalDateTime latestAt;

        private Group(String imageId) {
            this.imageId = imageId;
        }

        private void addRound(CopyHistoryEntity entity, List<String> outputs) {
            rounds.add(Map.of(
                    "id", entity.getId(),
                    "instruction", entity.getInstruction(),
                    "aiOutputs", outputs,
                    "sequenceNo", entity.getSequenceNo(),
                    "source", entity.getSource(),
                    "createdAt", entity.getCreatedAt() != null ? ISO.format(entity.getCreatedAt()) : null
            ));
            if (entity.getCreatedAt() != null) {
                latestAt = latestAt == null || entity.getCreatedAt().isAfter(latestAt)
                        ? entity.getCreatedAt()
                        : latestAt;
            }
        }

        private Map<String, Object> toMap() {
            return Map.of(
                    "imageId", imageId,
                    "rounds", rounds,
                    "latestAt", latestAt != null ? ISO.format(latestAt) : null
            );
        }
    }
}
