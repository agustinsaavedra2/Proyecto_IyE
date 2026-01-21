package com.backendie.controller;

import com.backendie.multitenancy.TenantContext;
import com.backendie.repository.UsuarioRepository;
import com.backendie.models.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
public class TenantController {

    private final UsuarioRepository usuarioRepository;

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        Long categoria = TenantContext.getCurrentCategoria();
        String email = authentication != null ? authentication.getName() : null;
        Usuario u = email != null ? usuarioRepository.findByEmail(email) : null;
        return ResponseEntity.ok(java.util.Map.of(
                "categoria", categoria,
                "usuario", u == null ? null : u.getEmail(),
                "rol", u == null ? null : u.getRol()
        ));
    }
}

