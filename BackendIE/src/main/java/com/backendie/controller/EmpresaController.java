package com.backendie.controller;

import com.backendie.dtos.EmpresaDTO;
import com.backendie.dtos.RegistrarEmpresa;
import com.backendie.models.Empresa;
import com.backendie.service.EmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @PostMapping("/registrar")
    public Empresa registrarEmpresa(@RequestBody RegistrarEmpresa empresa) {
        return empresaService.registrarEmpresa(
                empresa.getAdmin(),
                empresa.getCategoriaId(),
                empresa.getNombre(),
                empresa.getCodigoEmpresa(),
                empresa.getUbicacion(),
                empresa.getDescripcion()
        );
    }

    @GetMapping("/resumen")
    public List<EmpresaDTO> obtenerEmpresas() {
        return empresaService.obtenerEmpresasDTO();
    }
}
