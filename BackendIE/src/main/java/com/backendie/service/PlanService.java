package com.backendie.service;

import com.backendie.models.Plan;
import com.backendie.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    public Plan crearPlan(String nombre, Double precio, Integer maxUsuarios, Integer duracionMeses){
        if (nombre.isEmpty() || precio == null || maxUsuarios == null || duracionMeses == null) {
            throw new IllegalArgumentException("All fields are required");
        }
        Plan plan = new Plan(nombre, precio, maxUsuarios, duracionMeses);
        return planRepository.save(plan);
    }
}
