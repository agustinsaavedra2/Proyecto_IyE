package com.backendie.controller;

import com.backendie.models.Plan;
import com.backendie.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/planes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlanController {

    private final PlanService planService;

    @PostMapping("/crear")
    public Plan crear(@RequestBody Plan plan) {
        return planService.crearPlan(
                plan.getNombre(),
                plan.getPrecio(),
                plan.getMaxUsuarios(),
                plan.getDuracionMeses(),
                plan.getMaxConsultasMensuales(),
                plan.getUnlimited()
        );
    }

}
