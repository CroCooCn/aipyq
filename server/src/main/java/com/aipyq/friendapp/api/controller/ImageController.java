package com.aipyq.friendapp.api.controller;

import com.aipyq.friendapp.api.dto.RenderRequest;
import com.aipyq.friendapp.api.dto.RenderResult;
import com.aipyq.friendapp.api.dto.User;
import com.aipyq.friendapp.service.AuthService;
import com.aipyq.friendapp.service.ImageRenderService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/image")
public class ImageController {

    private final ImageRenderService imageRenderService;
    private final AuthService authService;

    public ImageController(ImageRenderService imageRenderService, AuthService authService) {
        this.imageRenderService = imageRenderService;
        this.authService = authService;
    }

    @PostMapping("/render")
    public ResponseEntity<RenderResult> render(@RequestBody RenderRequest request, HttpServletRequest http) {
        if (request == null || request.getImageUrl() == null || request.getImageUrl().isBlank()) {
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
        
        RenderResult result = imageRenderService.render(request);
        return ResponseEntity.ok(result);
    }
}

