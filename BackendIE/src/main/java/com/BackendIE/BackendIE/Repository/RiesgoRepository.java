package com.BackendIE.BackendIE.Repository;

import com.BackendIE.BackendIE.Models.Riesgo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RiesgoRepository extends JpaRepository<Riesgo, Long> {
}
