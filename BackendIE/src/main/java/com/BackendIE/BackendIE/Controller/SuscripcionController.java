package com.BackendIE.BackendIE.Controller;

import com.BackendIE.BackendIE.DTOs.Suscribirse;
import com.BackendIE.BackendIE.Models.Suscripcion;
import com.BackendIE.BackendIE.Service.SuscripcionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/suscripcion")
public class SuscripcionController {

    @Autowired
    private SuscripcionService suscripcionService;

    @PostMapping("/suscribirse")
    public Suscripcion suscribirse(@RequestBody Suscribirse suscripcion) {
        return suscripcionService.suscribirse(suscripcion.getEmpresaId(), suscripcion.getPlan(), suscripcion.getAdminId());
    }
}
