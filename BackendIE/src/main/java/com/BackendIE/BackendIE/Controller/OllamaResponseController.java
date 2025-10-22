package com.BackendIE.BackendIE.Controller;

import com.BackendIE.BackendIE.DTOs.CrearAuditoria;
import com.BackendIE.BackendIE.DTOs.CrearPPP;
import com.BackendIE.BackendIE.Models.OllamaResponse;
import com.BackendIE.BackendIE.Service.OllamaResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ollama")
@CrossOrigin(origins = "*")
public class OllamaResponseController {

    @Autowired
    private OllamaResponseService ollamaResponseService;

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

}
