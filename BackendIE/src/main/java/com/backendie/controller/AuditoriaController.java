package com.backendie.controller;

import com.backendie.models.Auditoria;
import com.backendie.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auditorias")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    // Crear nueva auditoría
    @PostMapping
    public ResponseEntity<Auditoria> createAuditoria(@RequestBody Auditoria auditoria) {
        Auditoria saved = auditoriaService.save(auditoria);
        return ResponseEntity.ok(saved);
    }

    // Listar todas
    @GetMapping
    public ResponseEntity<List<Auditoria>> getAllAuditorias() {
        return ResponseEntity.ok(auditoriaService.findAll());
    }

    // Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<Auditoria> getAuditoriaById(@PathVariable String id) {
        return auditoriaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Actualizar auditoría
    @PutMapping("/{id}")
    public ResponseEntity<Auditoria> updateAuditoria(
            @PathVariable String id,
            @RequestBody Auditoria auditoria
    ) {
        auditoria.setId(id);
        Auditoria updated = auditoriaService.save(auditoria);
        return ResponseEntity.ok(updated);
    }
}
