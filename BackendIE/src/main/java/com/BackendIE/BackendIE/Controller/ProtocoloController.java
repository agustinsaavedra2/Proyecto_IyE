package com.BackendIE.BackendIE.Controller;

import com.BackendIE.BackendIE.DTOs.CrearPPP;
import com.BackendIE.BackendIE.Models.Protocolo;
import com.BackendIE.BackendIE.Service.ProtocoloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ollama/protocolo")
public class ProtocoloController {

    @Autowired
    private ProtocoloService protocoloService;

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
