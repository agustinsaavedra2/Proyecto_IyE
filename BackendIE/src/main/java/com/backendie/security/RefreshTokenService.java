package com.backendie.security;

import com.backendie.models.RefreshToken;
import com.backendie.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${security.refresh-token.expiry-days:30}")
    private int refreshExpiryDays;

    private final SecureRandom secureRandom = new SecureRandom();

    public String createRefreshToken(Long userId) {
        // Generate random token string (not predictable)
        byte[] random = new byte[64];
        secureRandom.nextBytes(random);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(random);

        // hash token for storage
        String tokenHash = sha256(token);

        RefreshToken rt = RefreshToken.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusDays(refreshExpiryDays))
                .revoked(false)
                .createdAt(LocalDateTime.now()) // explicit: Lombok builder ignores field initializers
                .build();

        refreshTokenRepository.save(rt);
        return token;
    }

    public Optional<RefreshToken> findByToken(String token) {
        String hash = sha256(token);
        // Use the non-revoked finder for validation
        return refreshTokenRepository.findByTokenHashAndRevokedFalse(hash);
    }

    public void revoke(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    public void revokeAllForUser(Long userId) {
        var list = refreshTokenRepository.findAllByUserId(userId);
        for (RefreshToken r : list) {
            r.setRevoked(true);
        }
        refreshTokenRepository.saveAll(list);
    }

    public int deleteExpired() {
        return refreshTokenRepository.deleteExpired(LocalDateTime.now());
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to compute SHA-256 hash", e);
        }
    }
}
