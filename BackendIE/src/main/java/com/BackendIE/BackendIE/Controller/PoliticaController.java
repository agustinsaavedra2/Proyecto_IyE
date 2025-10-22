package com.BackendIE.BackendIE.Controller;

import com.BackendIE.BackendIE.DTOs.CrearPPP;
import com.BackendIE.BackendIE.Models.PoliticaEmpresa;
import com.BackendIE.BackendIE.Service.PoliticaEmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ollama/politica")
public class PoliticaController {

    @Autowired
    private PoliticaEmpresaService politicaService;



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
}
