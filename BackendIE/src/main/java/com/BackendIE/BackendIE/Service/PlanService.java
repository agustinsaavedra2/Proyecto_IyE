package com.BackendIE.BackendIE.Service;

import com.BackendIE.BackendIE.Models.Plan;
import com.BackendIE.BackendIE.Repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlanService {

    @Autowired
    private PlanRepository planRepository;

    public Plan crearPlan(String nombre, Double precio, Integer maxUsuarios, Integer duracionMeses){
        if (nombre.isEmpty() || precio == null || maxUsuarios == null || duracionMeses == null) {
            throw new IllegalArgumentException("All fields are required");
        }
        Plan plan = new Plan(nombre, precio, maxUsuarios, duracionMeses);
        return planRepository.save(plan);
    }
}
