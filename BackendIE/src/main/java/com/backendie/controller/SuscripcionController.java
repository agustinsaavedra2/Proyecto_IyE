package com.backendie.controller;

import com.backendie.dtos.Suscribirse;
import com.backendie.models.Suscripcion;
import com.backendie.service.SuscripcionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/suscripcion")
@RequiredArgsConstructor
public class SuscripcionController {

    private final SuscripcionService suscripcionService;

    @PostMapping("/suscribirse")
    public Suscripcion suscribirse(@RequestBody Suscribirse suscripcion) {
        return suscripcionService.suscribirse(suscripcion.getEmpresaId(), suscripcion.getPlan(), suscripcion.getAdminId());
    }
}
