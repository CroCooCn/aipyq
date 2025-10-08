package com.aipyq.friendapp.api.controller;

import com.aipyq.friendapp.api.dto.AnonymousSession;
import com.aipyq.friendapp.api.dto.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/anonymous")
    public ResponseEntity<AnonymousSession> anonymous() {
        AnonymousSession s = new AnonymousSession();
        s.setToken("anon-" + UUID.randomUUID());
        s.setExpiresAt(OffsetDateTime.now().plusDays(7));
        return ResponseEntity.ok(s);
    }

    public static class LoginReq { public String phone; public String code; }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody LoginReq req) {
        User u = new User();
        u.setId(UUID.randomUUID().toString());
        u.setPhone(req.phone);
        u.setPlanId(null);
        u.setRemainingQuota(30);
        return ResponseEntity.ok(u);
    }
}

