package com.backendie.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMillis;

    public JwtUtil(@Value("${token.secret}") String base64Secret,
                   @Value("${token.expiration-minutes:60}") long expirationMinutes) {
        byte[] decode = Base64.getDecoder().decode(base64Secret);
        this.key = Keys.hmacShaKeyFor(decode);
        this.expirationMillis = expirationMinutes * 60 * 1000;
    }

    public String generateToken(String correo, List<String> roles){
        return generateToken(correo, roles, 0);
    }

    public String generateToken(String correo, List<String> roles, int tokenVersion){
        long now = System.currentTimeMillis();
        var builder = Jwts.builder()
                .setSubject(correo)
                .claim("tokenVersion", tokenVersion)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMillis))
                .signWith(key, SignatureAlgorithm.HS256);

        if (roles != null && !roles.isEmpty()){
            builder.claim("roles", roles);
        }

        return builder.compact();
    }

    public long getExpirationMillis() {
        return expirationMillis;
    }

    // New helper: return expiration in seconds (useful for API responses)
    public long getExpirationSeconds() {
        return expirationMillis / 1000L;
    }
}
