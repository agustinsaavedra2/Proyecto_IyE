package com.backendie.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.backendie.repository.UsuarioRepository;
import com.backendie.models.Usuario;

import java.io.IOException;

@Component
public class TokenVersionFilter extends OncePerRequestFilter {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        // If no authentication present, skip
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // If authentication is a JwtAuthenticationToken, get claim directly from Jwt
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                Object claim = jwt.getClaims().get("tokenVersion");
                int tokenVersion = 0;
                if (claim instanceof Number) tokenVersion = ((Number) claim).intValue();
                else if (claim instanceof String) {
                    try { tokenVersion = Integer.parseInt((String) claim); } catch (NumberFormatException ignored) {}
                }

                Usuario u = usuarioRepository.findByEmail(auth.getName());
                if (u != null) {
                    int current = u.getTokenVersion() == null ? 0 : u.getTokenVersion();
                    if (tokenVersion != current) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("Token version mismatch");
                        return;
                    }
                }
            }
        } catch (Exception e) {
            // If anything goes wrong, fail closed by returning 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token validation error");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
