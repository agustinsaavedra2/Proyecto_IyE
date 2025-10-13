package com.BackendIE.BackendIE.Repository;

import com.BackendIE.BackendIE.Models.ControlEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ControlEmpresaRepository extends JpaRepository<ControlEmpresa, Long> {
}
