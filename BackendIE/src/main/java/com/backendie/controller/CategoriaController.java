package com.backendie.controller;

import com.backendie.dtos.CategoriaDTO;
import com.backendie.dtos.CategoriaRegulacionDTO;
import com.backendie.models.CategoriaIndustria;
import com.backendie.service.CategoriaIndustriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaIndustriaService categoriaIndustriaService;

    @PostMapping("/crear")
    public CategoriaIndustria crearCategoria(@RequestBody CategoriaIndustria categoriaIndustria) {
        return categoriaIndustriaService.crearCategoria(categoriaIndustria.getNombre(), categoriaIndustria.getDescripcion(), categoriaIndustria.getRegulaciones());
    }

    @GetMapping("/dto")
    public Iterable<CategoriaDTO> listarCategorias() {
        return categoriaIndustriaService.listarCategorias();
    }

    @GetMapping("listar")
    public Iterable<CategoriaRegulacionDTO> listarTodasCategorias() {
        return categoriaIndustriaService.listarTodasCategorias();
    }

}
