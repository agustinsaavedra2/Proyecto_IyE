package com.backendie.controller;

import com.backendie.dtos.CrearRiesgo;
import com.backendie.models.OllamaResponse;
import com.backendie.models.Riesgo;
import com.backendie.service.RiesgoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/riesgos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RiesgoController {

    private final RiesgoService riesgoService;

    // Crear nuevo riesgo
    @PostMapping
    public ResponseEntity<Riesgo> crearRiesgo(@RequestBody Riesgo riesgo) {
        Riesgo nuevo = riesgoService.save(riesgo);
        return ResponseEntity.ok(nuevo);
    }

    // Obtener todos los riesgos
    @GetMapping
    public ResponseEntity<List<Riesgo>> obtenerTodos() {
        return ResponseEntity.ok(riesgoService.findAll());
    }

    // Obtener un riesgo por ID
    @GetMapping("/{id}")
    public ResponseEntity<Riesgo> obtenerPorId(@PathVariable String id) {
        Riesgo riesgo = riesgoService.findById(id);
        return (riesgo != null) ? ResponseEntity.ok(riesgo) : ResponseEntity.notFound().build();
    }

    // Actualizar un riesgo
    @PutMapping("/{id}")
    public ResponseEntity<Riesgo> actualizarRiesgo(@PathVariable String id, @RequestBody Riesgo riesgoActualizado) {
        Riesgo existente = riesgoService.findById(id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }

        riesgoActualizado.setId(id);
        Riesgo actualizado = riesgoService.save(riesgoActualizado);
        return ResponseEntity.ok(actualizado);
    }

    // Eliminar un riesgo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRiesgo(@PathVariable String id) {
        Riesgo existente = riesgoService.findById(id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }

        riesgoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // --- Generación automática de riesgos con Ollama ---
    @PostMapping("/generar")
    public OllamaResponse generarRiesgos(@RequestBody CrearRiesgo dto) {
        return riesgoService.generarRiesgos(dto.getEmpresaId(), dto.getUsuarioId(), dto.getIdsDePoliticas());
    }
}
