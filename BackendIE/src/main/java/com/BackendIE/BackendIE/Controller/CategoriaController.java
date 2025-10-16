package com.BackendIE.BackendIE.Controller;

import com.BackendIE.BackendIE.Models.CategoriaIndustria;
import com.BackendIE.BackendIE.Service.CategoriaIndustriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*")
public class CategoriaController {

    @Autowired
    private CategoriaIndustriaService categoriaIndustriaService;

    @PostMapping("/crear")
    public CategoriaIndustria crearCategoria(@RequestBody CategoriaIndustria categoriaIndustria) {
        return categoriaIndustriaService.crearCategoria(categoriaIndustria.getNombre(), categoriaIndustria.getDescripcion(), categoriaIndustria.getRegulaciones());
    }

}
