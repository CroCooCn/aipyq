package com.aipyq.friendapp.api.controller;

import com.aipyq.friendapp.api.dto.User;
import com.aipyq.friendapp.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public record SendCodeRequest(String phone) {}
    public record LoginRequest(String phone, String code) {}
    public record LoginResponse(String token, User user, String message) {}

    @PostMapping("/send-code")
    public ResponseEntity<Void> sendCode(@RequestBody SendCodeRequest request) {
        if (request == null || request.phone == null || request.phone.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            authService.sendLoginCode(request.phone);
            return ResponseEntity.noContent().build();
        } catch (AuthService.AuthException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        if (request == null || request.phone == null || request.phone.isBlank()
                || request.code == null || request.code.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            AuthService.LoginSession session = authService.login(request.phone, request.code);
            return ResponseEntity.ok(new LoginResponse(session.token(), copyUser(session.user()), null));
        } catch (AuthService.AuthException ex) {
            return ResponseEntity.status(401).body(new LoginResponse(null, null, ex.getMessage()));
        }
    }

    private User copyUser(User source) {
        if (source == null) {
            return null;
        }
        User user = new User();
        user.setId(source.getId());
        user.setPhone(source.getPhone());
        user.setPlanId(source.getPlanId());
        user.setRemainingQuota(source.getRemainingQuota());
        return user;
    }
}
