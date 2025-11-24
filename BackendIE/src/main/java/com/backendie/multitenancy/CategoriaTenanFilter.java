package com.backendie.multitenancy;

import com.backendie.service.UsuarioService;
import com.backendie.repository.EmpresaRepository;
import com.backendie.models.Empresa;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CategoriaTenanFilter extends OncePerRequestFilter {

    private final UsuarioService usuarioService;
    private final TenantHibernateFilter tenantHibernateFilter;
    private final EmpresaRepository empresaRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException{
        try{
            String headerCategoria = request.getHeader("X-Categoria-id");
            if (headerCategoria != null && !headerCategoria.isBlank())  {
                try {
                    // Try to interpret header as empresaId first; if found, use its categoriaId
                    Long parsed = Long.valueOf(headerCategoria);
                    Empresa e = empresaRepository.findById(parsed).orElse(null);
                    if (e != null) {
                        TenantContext.setCurrentCategoria(e.getCategoriaId());
                    } else {
                        // Not an empresa id, treat as categoria id
                        TenantContext.setCurrentCategoria(parsed);
                    }
                } catch (NumberFormatException ignored){
                    // if it's not numeric, ignore and continue
                }
            } else {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
                    Long categoriaId = usuarioService.getCategoriaIdByEmail(auth.getName());
                    if (categoriaId != null) {
                        TenantContext.setCurrentCategoria(categoriaId);
                    }
                }
            }
            // enable hibernate filter for JPA queries
            Long cat = TenantContext.getCurrentCategoria();
            if (cat != null) {
                tenantHibernateFilter.enableCategoriaFilter(cat);
            }

            filterChain.doFilter(request, response);
        } finally {
            tenantHibernateFilter.disableCategoriaFilter();
            TenantContext.clear();
        }
    }

}
