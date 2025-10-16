package com.BackendIE.BackendIE.Controller;

import com.BackendIE.BackendIE.Models.Plan;
import com.BackendIE.BackendIE.Repository.PlanRepository;
import com.BackendIE.BackendIE.Service.PlanService;
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
