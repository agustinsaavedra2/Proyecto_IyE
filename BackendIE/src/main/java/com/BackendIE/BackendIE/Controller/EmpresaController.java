package com.BackendIE.BackendIE.Controller;

import com.BackendIE.BackendIE.DTOs.RegistrarEmpresa;
import com.BackendIE.BackendIE.Models.Empresa;
import com.BackendIE.BackendIE.Service.EmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/empresas")
@CrossOrigin(origins = "*")
public class EmpresaController {

    @Autowired
    private EmpresaService empresaService;

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

}
