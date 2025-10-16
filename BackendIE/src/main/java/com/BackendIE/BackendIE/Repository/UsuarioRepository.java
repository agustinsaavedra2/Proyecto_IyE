package com.BackendIE.BackendIE.Repository;

import com.BackendIE.BackendIE.Models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Usuario findByEmail(String email);

    List<Usuario> findByEmpresaId(Long empresaId);
}
