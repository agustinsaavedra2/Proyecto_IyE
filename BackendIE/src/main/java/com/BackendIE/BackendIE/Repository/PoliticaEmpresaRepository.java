package com.BackendIE.BackendIE.Repository;

import com.BackendIE.BackendIE.Models.PoliticaEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PoliticaEmpresaRepository extends JpaRepository<PoliticaEmpresa, Long> {
}
