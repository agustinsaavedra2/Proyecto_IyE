package com.BackendIE.BackendIE.Repository;

import com.BackendIE.BackendIE.Models.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    Plan findByNombre(String nombre);
}
