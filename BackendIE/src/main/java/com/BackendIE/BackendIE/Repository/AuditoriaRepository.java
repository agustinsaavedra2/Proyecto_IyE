package com.BackendIE.BackendIE.Repository;

import com.BackendIE.BackendIE.Models.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long>{

}
