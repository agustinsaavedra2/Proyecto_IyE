package com.backendie.service;

import com.backendie.models.Plan;
import com.backendie.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;

    public Plan crearPlan(String nombre, Double precio, Integer maxUsuarios, Integer duracionMeses, Integer maxConsultasMensuales, Boolean unlimited) {
        if (nombre.isBlank() || nombre == null) throw new IllegalArgumentException("Nombre es requerido");
        Objects.requireNonNull(precio, "precio es requerido");
        if (precio < 0) throw new IllegalArgumentException("precio debe ser positivo");

        checkPositive("Usuarios maximos", maxUsuarios);
        checkPositive("Duracion en meses", duracionMeses);
        if (!unlimited) {
            checkPositive("Consultas mensuales maximas", maxConsultasMensuales);
        }

        Plan plan = Plan.builder()
                .nombre(nombre)
                .precio(precio)
                .maxUsuarios(maxUsuarios)
                .duracionMeses(duracionMeses)
                .maxConsultasMensuales(maxConsultasMensuales)
                .unlimited(unlimited)
                .build();
        return planRepository.save(plan);
    }

    private void checkPositive(String fieldName, Integer value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " debe ser un número positivo");
        }
    }
}
