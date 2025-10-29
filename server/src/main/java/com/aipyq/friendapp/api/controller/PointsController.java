package com.aipyq.friendapp.api.controller;

import com.aipyq.friendapp.api.dto.User;
import com.aipyq.friendapp.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/points")
public class PointsController {

    private final AuthService authService;

    public PointsController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Integer>> get(HttpServletRequest http) {
        Optional<User> userOpt = authService.findUserByAuthorization(http.getHeader("Authorization"));
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        return authService.findUserById(userOpt.get().getId())
                .map(user -> ResponseEntity.ok(Map.of(
                        "remaining", user.getRemainingQuota() == null ? 0 : user.getRemainingQuota()
                )))
                .orElseGet(() -> ResponseEntity.status(404).build());
    }

    public record RechargeReq(double amountYuan) {}

    @PostMapping("/recharge")
    public ResponseEntity<Map<String, Object>> recharge(@RequestBody RechargeReq req, HttpServletRequest http) {
        if (req == null || req.amountYuan <= 0) {
            return ResponseEntity.badRequest().build();
        }
        Optional<User> userOpt = authService.findUserByAuthorization(http.getHeader("Authorization"));
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        int points = (int) Math.floor(req.amountYuan / 0.1d);
        if (points <= 0) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "金额过小，无法兑换点券"));
        }
        Optional<User> updated = authService.addQuota(userOpt.get().getId(), points);
        if (updated.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("ok", false, "message", "用户不存在"));
        }
        int remaining = updated.get().getRemainingQuota() == null ? 0 : updated.get().getRemainingQuota();
        return ResponseEntity.ok(Map.of("ok", true, "added", points, "remaining", remaining));
    }
}
