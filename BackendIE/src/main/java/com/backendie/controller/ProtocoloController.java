package com.backendie.controller;

import com.backendie.dtos.CrearPPP;
import com.backendie.models.Protocolo;
import com.backendie.service.ProtocoloService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ollama/protocolo")
public class ProtocoloController {

    private final ProtocoloService protocoloService;

    @PostMapping("/crear")
    public ResponseEntity<?> crearProtocolo(@RequestBody CrearPPP dto) {
        try {
            Protocolo protocolo = protocoloService.crearProtocolo(
                    dto.getEmpresaId(),
                    dto.getUsuarioId(),
                    dto.getPoliticaId(),
                    dto.getPregunta()
            );
            return ResponseEntity.ok(protocolo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear el protocolo: " + e.getMessage());
        }
    }
}
