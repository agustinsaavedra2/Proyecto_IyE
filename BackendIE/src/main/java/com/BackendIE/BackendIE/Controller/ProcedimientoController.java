package com.BackendIE.BackendIE.Controller;

import com.BackendIE.BackendIE.DTOs.CrearPPP;
import com.BackendIE.BackendIE.Models.Procedimiento;
import com.BackendIE.BackendIE.Service.ProcedimientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ollama/procedimiento")
public class ProcedimientoController {

    @Autowired
    private ProcedimientoService procedimientoService;

    @PostMapping("/crear")
    public ResponseEntity<?> crearProcedimiento(@RequestBody CrearPPP dto) {
        try {
            Procedimiento procedimiento = procedimientoService.crearProcedimiento(
                    dto.getEmpresaId(),
                    dto.getUsuarioId(),
                    dto.getProtocoloId(),
                    dto.getPregunta()
            );
            return ResponseEntity.ok(procedimiento);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear el procedimiento: " + e.getMessage());
        }
    }
}
