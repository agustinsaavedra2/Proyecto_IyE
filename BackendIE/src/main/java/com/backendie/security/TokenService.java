package com.backendie.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Component
public class TokenService {
    private final SecretKey key;
    private final JwtParser parser;
    private final long validityMs;

    public TokenService(@Value("${token.secret}") String secret,
                        @Value("${token.email.expMinutes:30}") long expMinutes) {
        byte[] keyBytes = tryDecodeBase64(secret);
        if (keyBytes == null) keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("token.secret must be at least 32 bytes long (or provide a base64-encoded secret)");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.parser = Jwts.parser().verifyWith(this.key).build();
        this.validityMs = expMinutes * 60 * 1000;
    }

    private byte[] tryDecodeBase64(String s) {
        return Base64.getDecoder().decode(s);
    }

    /**
     * Genera un JWT firmado con HS256 que contiene el userId en el `sub` y el claim `purpose=email_verification`.
     */
    public String generateEmailVerificationToken(Long userId) {
        Instant now = Instant.now();
        Date iat = Date.from(now);
        Date exp = Date.from(now.plusMillis(validityMs));

        return Jwts.builder()
                .subject(Long.toString(userId))
                .claim("purpose", "email_verification")
                .issuedAt(iat)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    public Long validateAndExtractUserId(String token) throws JwtException {
        Jws<Claims> jws = parser.parseSignedClaims(token);
        Claims claims = jws.getPayload();
        Object purpose = claims.get("purpose");
        if (purpose == null || !"email_verification".equals(purpose.toString())) {
            throw new JwtException("Invalid token purpose");
        }
        String sub = claims.getSubject();
        if (sub == null) throw new JwtException("Missing subject");
        try {
            return Long.parseLong(sub);
        } catch (NumberFormatException ex) {
            throw new JwtException("Invalid subject");
        }
    }
}