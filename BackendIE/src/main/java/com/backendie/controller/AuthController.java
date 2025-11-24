package com.backendie.controller;

import com.backendie.dtos.JwtResponse;
import com.backendie.dtos.Loginuserdto;
import com.backendie.models.RefreshToken;
import com.backendie.models.Usuario;
import com.backendie.security.JwtUtil;
import com.backendie.security.RefreshTokenService;
import com.backendie.service.UsuarioService;
import com.backendie.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody Loginuserdto dto) {
        Usuario usuario = usuarioService.authenticate(dto.getEmail(), dto.getPassword());
        if (usuario == null) return ResponseEntity.status(401).build();
        List<String> roles = usuario.getRol() != null ? List.of(usuario.getRol()) : List.of();
        int tokenVersion = usuario.getTokenVersion() == null ? 0 : usuario.getTokenVersion();
        String token = jwtUtil.generateToken(usuario.getEmail(), roles, tokenVersion);
        String refresh = refreshTokenService.createRefreshToken(usuario.getId());
        JwtResponse resp = JwtResponse.builder()
                .token(token)
                .refreshToken(refresh)
                .expiresIn(jwtUtil.getExpirationSeconds())
                .build();
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@RequestParam("refreshToken") String refreshToken) {
        var opt = refreshTokenService.findByToken(refreshToken);
        if (opt.isEmpty()) return ResponseEntity.status(401).build();
        RefreshToken rt = opt.get();
        if (rt.isRevoked() || rt.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            return ResponseEntity.status(401).build();
        }
        // fetch user
        Usuario usuario = usuarioService.getById(rt.getUserId());
        if (usuario == null) return ResponseEntity.status(401).build();
        String token = jwtUtil.generateToken(usuario.getEmail(), List.of(usuario.getRol()), usuario.getTokenVersion() == null ? 0 : usuario.getTokenVersion());
        // Optionally rotate refresh token: revoke old and create new
        refreshTokenService.revoke(rt);
        String newRefresh = refreshTokenService.createRefreshToken(usuario.getId());
        JwtResponse resp = JwtResponse.builder().token(token).refreshToken(newRefresh).expiresIn(jwtUtil.getExpirationSeconds()).build();
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam("refreshToken") String refreshToken) {
        var opt = refreshTokenService.findByToken(refreshToken);
        if (opt.isPresent()) {
            RefreshToken rt = opt.get();
            // revoke the provided refresh token
            refreshTokenService.revoke(rt);
            // also revoke all refresh tokens for this user and bump tokenVersion to invalidate existing JWTs
            try {
                Usuario u = usuarioRepository.findById(rt.getUserId()).orElse(null);
                if (u != null) {
                    Integer current = u.getTokenVersion() == null ? 0 : u.getTokenVersion();
                    u.setTokenVersion(current + 1);
                    usuarioRepository.save(u);
                    refreshTokenService.revokeAllForUser(u.getId());
                }
            } catch (Exception ignored) {}
        }
        return ResponseEntity.ok().build();
    }

    // Revoke all active sessions for the authenticated user (logout current user everywhere)
    @PostMapping("/logout-me")
    public ResponseEntity<Void> logoutMe(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        String email = authentication.getName();
        Usuario u = usuarioRepository.findByEmail(email);
        if (u == null) return ResponseEntity.notFound().build();
        // increment token version to invalidate existing JWTs
        Integer current = u.getTokenVersion() == null ? 0 : u.getTokenVersion();
        u.setTokenVersion(current + 1);
        usuarioRepository.save(u);
        // revoke all refresh tokens for user
        refreshTokenService.revokeAllForUser(u.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(@RequestParam("email") String email) {
        Usuario u = usuarioRepository.findByEmail(email);
        if (u == null) return ResponseEntity.notFound().build();
        // increment token version to invalidate existing JWTs
        Integer current = u.getTokenVersion() == null ? 0 : u.getTokenVersion();
        u.setTokenVersion(current + 1);
        usuarioRepository.save(u);
        // revoke all refresh tokens for user
        refreshTokenService.revokeAllForUser(u.getId());
        return ResponseEntity.ok().build();
    }
}
