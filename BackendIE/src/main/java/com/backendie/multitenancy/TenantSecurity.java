package com.backendie.multitenancy;

import com.backendie.models.Empresa;
import com.backendie.models.Usuario;
import com.backendie.repository.EmpresaRepository;
import com.backendie.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantSecurity {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;

    public boolean empresaBelongsToCurrentCategoria(Long empresaId) {
        if (empresaId == null) return false;
        Long current = TenantContext.getCurrentCategoria();
        if (current == null) return false;
        Empresa e = empresaRepository.findById(empresaId).orElse(null);
        if (e == null) return false;
        // allow if empresa's categoria matches current
        if (current.equals(e.getCategoriaId())) return true;
        // allow if authenticated user is admin -> elevated cross-tenant action
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
            Usuario u = usuarioRepository.findByEmail(auth.getName());
            if (u != null && "admin".equalsIgnoreCase(u.getRol())) {
                return true;
            }
        }
        return false;
    }

    public void assertEmpresaBelongsToCurrentCategoria(Long empresaId) {
        if (!empresaBelongsToCurrentCategoria(empresaId)) {
            throw new SecurityException("Empresa does not belong to current tenant category or tenant not set");
        }
    }
}
