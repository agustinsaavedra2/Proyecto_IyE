package com.backendie.controller;

import com.backendie.dtos.CrearPPP;
import com.backendie.dtos.PoliticaDTO;
import com.backendie.models.PoliticaEmpresa;
import com.backendie.service.PoliticaEmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ollama/politica")
public class PoliticaController {

    private final PoliticaEmpresaService politicaService;

    @PostMapping("/crear")
    public ResponseEntity<?> crearPolitica(@RequestBody CrearPPP dto) {
        try {
            PoliticaEmpresa politica = politicaService.crearPolitica(
                    dto.getEmpresaId(),
                    dto.getUsuarioId(),
                    dto.getPregunta()
            );
            return ResponseEntity.ok(politica);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear la política: " + e.getMessage());
        }
    }

    @GetMapping("/politicaempresa")
    public List<PoliticaDTO> obtenerPoliticasPorEmpresa(@RequestParam Long empresaId) {
        return politicaService.obtenerPoliticasPorEmpresa(empresaId);
    }
}
