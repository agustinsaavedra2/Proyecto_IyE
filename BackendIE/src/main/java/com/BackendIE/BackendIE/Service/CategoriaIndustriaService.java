package com.BackendIE.BackendIE.Service;

import com.BackendIE.BackendIE.Models.CategoriaIndustria;
import com.BackendIE.BackendIE.Repository.CategoriaIndustriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaIndustriaService {

    @Autowired
    private CategoriaIndustriaRepository categoriaIndustriaRepository;

    public CategoriaIndustria crearCategoria(String nombre, String descripcion, List<String> regulaciones) {
        if (nombre == null || nombre.isEmpty() || descripcion == null || descripcion.isEmpty()) {
            throw new IllegalArgumentException("El nombre y la descripción no pueden estar vacíos");
        }
        CategoriaIndustria categoria = new CategoriaIndustria(nombre, descripcion, regulaciones);
        return categoriaIndustriaRepository.save(categoria);
    }
}
