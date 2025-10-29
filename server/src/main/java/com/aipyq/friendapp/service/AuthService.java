package com.aipyq.friendapp.service;

import com.aipyq.friendapp.api.dto.User;
import com.aipyq.friendapp.persistence.entity.UserEntity;
import com.aipyq.friendapp.persistence.mapper.UserMapper;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final int DEFAULT_QUOTA = 80;

    private final UserMapper userMapper;

    private final Map<String, PendingCode> pendingCodes = new ConcurrentHashMap<>();

    public AuthService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public void sendLoginCode(String phoneRaw) {
        String phone = normalizePhone(phoneRaw);
        if (!StringUtils.hasText(phone)) {
            throw new AuthException("Phone number required");
        }
        String code = String.format(Locale.ROOT, "%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        pendingCodes.put(phone, new PendingCode(code, Instant.now().plus(CODE_TTL)));
        log.info("Login code for {} is {}", phone, code);
    }

    public LoginSession login(String phoneRaw, String codeRaw) {
        String phone = normalizePhone(phoneRaw);
        if (!StringUtils.hasText(phone)) {
            throw new AuthException("Phone number required");
        }
        if (!StringUtils.hasText(codeRaw)) {
            throw new AuthException("Verification code required");
        }
        PendingCode pending = pendingCodes.get(phone);
        if (pending == null || pending.isExpired()) {
            throw new AuthException("Verification code expired");
        }
        if (!pending.code.equals(codeRaw.trim())) {
            throw new AuthException("Verification code invalid");
        }
        pendingCodes.remove(phone);

        UserEntity entity = userMapper.findByPhone(phone);
        String newSecret = generateSecret();
        if (entity == null) {
            entity = new UserEntity();
            entity.setId(UUID.randomUUID().toString());
            entity.setPhone(phone);
            entity.setPlanId(null);
            entity.setRemainingQuota(DEFAULT_QUOTA);
            entity.setAuthSecret(newSecret);
            LocalDateTime now = LocalDateTime.now();
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            userMapper.insert(entity);
        } else {
            userMapper.updateAuthSecret(entity.getId(), newSecret);
            entity.setAuthSecret(newSecret);
        }

        User user = toDto(entity);
        String token = buildToken(user.getId(), entity.getAuthSecret());
        return new LoginSession(token, user);
    }

    public Optional<User> findUserByAuthorization(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader)) {
            return Optional.empty();
        }
        String token = authorizationHeader.trim();
        if (token.toLowerCase(Locale.ROOT).startsWith("bearer ")) {
            token = token.substring(7);
        }
        if (!StringUtils.hasText(token)) {
            return Optional.empty();
        }
        return findUserByToken(token);
    }

    public Optional<User> findUserByToken(String token) {
        if (!StringUtils.hasText(token)) {
            return Optional.empty();
        }
        ParsedToken parsed = parseToken(token.trim());
        if (parsed == null) {
            return Optional.empty();
        }
        UserEntity entity = userMapper.selectById(parsed.userId());
        if (entity == null || !StringUtils.hasText(entity.getAuthSecret())) {
            return Optional.empty();
        }
        if (!entity.getAuthSecret().equals(parsed.secret())) {
            return Optional.empty();
        }
        return Optional.of(toDto(entity));
    }

    public Optional<User> findUserById(String userId) {
        if (!StringUtils.hasText(userId)) {
            return Optional.empty();
        }
        UserEntity entity = userMapper.selectById(userId);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(toDto(entity));
    }

    public Optional<User> deductQuota(String userId, int cost) {
        if (cost <= 0) {
            return findUserById(userId);
        }
        int affected = userMapper.decreaseQuota(userId, cost);
        if (affected == 0) {
            return Optional.empty();
        }
        return findUserById(userId);
    }

    public Optional<User> addQuota(String userId, int points) {
        if (points <= 0) {
            return findUserById(userId);
        }
        userMapper.increaseQuota(userId, points);
        return findUserById(userId);
    }

    private String buildToken(String userId, String secret) {
        return userId + "." + secret;
    }

    private User toDto(UserEntity entity) {
        User user = new User();
        user.setId(entity.getId());
        user.setPhone(entity.getPhone());
        user.setPlanId(entity.getPlanId());
        user.setRemainingQuota(entity.getRemainingQuota());
        return user;
    }

    private String normalizePhone(String phone) {
        return phone == null ? null : phone.trim();
    }

    private String generateSecret() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private ParsedToken parseToken(String token) {
        String trimmed = token.trim();
        int dot = trimmed.indexOf('.');
        if (dot <= 0 || dot >= trimmed.length() - 1) {
            return null;
        }
        String userId = trimmed.substring(0, dot);
        String secret = trimmed.substring(dot + 1);
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(secret)) {
            return null;
        }
        return new ParsedToken(userId, secret);
    }

    private record PendingCode(String code, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    public record LoginSession(String token, User user) {}

    private record ParsedToken(String userId, String secret) {}

    public static class AuthException extends RuntimeException {
        public AuthException(String message) {
            super(message);
        }
    }
}
