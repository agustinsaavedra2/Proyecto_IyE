package com.backendie.controller;

import com.backendie.dtos.CrearAuditoria;
import com.backendie.models.OllamaResponse;
import com.backendie.service.OllamaResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ollama")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class OllamaResponseController {

    private final OllamaResponseService ollamaResponseService;

    @GetMapping
    public List<OllamaResponse> getAll() {
        return ollamaResponseService.getAll();
    }

    @GetMapping("/{id}")
    public OllamaResponse getById(@PathVariable String id) {
        return ollamaResponseService.getById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró la respuesta con id " + id));
    }

    @PostMapping
    public OllamaResponse create(@RequestBody OllamaResponse response) {
        return ollamaResponseService.save(response);
    }

    @PutMapping("/{id}")
    public OllamaResponse update(@PathVariable String id, @RequestBody OllamaResponse updatedResponse) {
        return ollamaResponseService.update(id, updatedResponse);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        ollamaResponseService.delete(id);
    }


    @PostMapping("/crearAuditoria")
    public ResponseEntity<?> crearAuditoria(@RequestBody CrearAuditoria auditoria) {
        try {
            OllamaResponse resp = ollamaResponseService.crearAuditoria(
                    auditoria.getEmpresaId(),
                    auditoria.getTipo(),
                    auditoria.getObjetivo(),
                    auditoria.getAuditorLiderId(),
                    auditoria.getIdsDePoliticasAEvaluar() // <-- ya es List<String>
            );
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body("Error al crear auditoría: " + e.getMessage());
        }
    }

    @PostMapping("/crearPPP")
    public ResponseEntity<?> crearPPP(@RequestBody com.backendie.dtos.CrearPPP dto) {
        try {
            OllamaResponse resp = ollamaResponseService.crearPPP(dto.getEmpresaId(), dto.getUsuarioId(), dto.getPregunta());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al crear PPP: " + e.getMessage());
        }
    }

    @PostMapping("/classifyRisk")
    public ResponseEntity<?> classifyRisk(@RequestBody com.backendie.dtos.CrearRiesgo dto) {
        try {
            // dto.idsDePoliticas is List<Long> in CrearRiesgo; convert to List<String> if needed
            List<String> idsStr = dto.getIdsDePoliticas() == null ? List.of() : dto.getIdsDePoliticas().stream().map(Object::toString).toList();
            Map<String,Object> result = ollamaResponseService.classifyRisk(dto.getEmpresaId(), dto.getUsuarioId(), idsStr);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al clasificar riesgo: " + e.getMessage());
        }
    }

    @PostMapping("/ner")
    public ResponseEntity<?> ner(@RequestBody Map<String,String> body) {
        try {
            String text = body.getOrDefault("text", "");
            Map<String,Object> out = ollamaResponseService.extractEntities(text);
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error en NER: " + e.getMessage());
        }
    }

}