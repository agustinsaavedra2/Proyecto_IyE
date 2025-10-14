package com.BackendIE.BackendIE.Controller;

import com.BackendIE.BackendIE.Models.Auditoria;
import com.BackendIE.BackendIE.Service.AuditoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auditorias")
@CrossOrigin(origins = "*")
public class AuditoriaController {

    @Autowired
    private AuditoriaService auditoriaService;

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
