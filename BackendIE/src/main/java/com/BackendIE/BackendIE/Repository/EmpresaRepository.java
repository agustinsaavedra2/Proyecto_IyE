package com.BackendIE.BackendIE.Repository;

import com.BackendIE.BackendIE.Models.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Empresa findById(long id);

}
