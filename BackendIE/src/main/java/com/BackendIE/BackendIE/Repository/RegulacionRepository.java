package com.BackendIE.BackendIE.Repository;

import com.BackendIE.BackendIE.Models.Regulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegulacionRepository extends JpaRepository<Regulacion, Long> {
}
