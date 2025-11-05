package com.backendie.controller;

import com.backendie.models.Plan;
import com.backendie.service.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/planes")
@CrossOrigin(origins = "*")
public class PlanController {

    @Autowired
    private PlanService planService;

    @PostMapping("/crear")
    public Plan crear(@RequestBody Plan plan) {
        return planService.crearPlan(
                plan.getNombre(),
                plan.getPrecio(),
                plan.getMaxUsuarios(),
                plan.getDuracionMeses()
        );
    }

}
