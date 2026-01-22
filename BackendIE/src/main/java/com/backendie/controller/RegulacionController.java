package com.backendie.controller;

import com.backendie.models.Regulacion;
import com.backendie.service.RegulacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/regulaciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RegulacionController {

    private final RegulacionService regulacionService;

    // Listar todas
    @GetMapping("/all")
    public ResponseEntity<List<Regulacion>> getAll() {
        return ResponseEntity.ok(regulacionService.findAll());
    }

    // Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<Regulacion> getById(@PathVariable String id) {
        return regulacionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Crear
    @PostMapping
    public ResponseEntity<Regulacion> create(@RequestBody Regulacion regulacion) {
        return ResponseEntity.ok(regulacionService.save(regulacion));
    }

    // Actualizar
    @PutMapping("/{id}")
    public ResponseEntity<Regulacion> update(@PathVariable String id, @RequestBody Regulacion updated) {
        return ResponseEntity.ok(regulacionService.update(id, updated));
    }

    // Eliminar (hard delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        regulacionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/crear")
    public Regulacion crearRegulacion(@RequestBody Regulacion regulacion) {
        return regulacionService.crearRegulacion(
                regulacion.getNombre(),
                regulacion.getContenido(),
                regulacion.getUrlDocumento(),
                regulacion.getEntidadEmisora(),
                regulacion.getAnioEmision()
        );
    }
}
