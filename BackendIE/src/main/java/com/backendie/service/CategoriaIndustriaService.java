package com.backendie.service;

import com.backendie.dtos.CategoriaDTO;
import com.backendie.dtos.CategoriaRegulacionDTO;
import com.backendie.models.CategoriaIndustria;
import com.backendie.repository.CategoriaIndustriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaIndustriaService {

    private final CategoriaIndustriaRepository categoriaIndustriaRepository;

    public CategoriaIndustria crearCategoria(String nombre, String descripcion, List<String> regulaciones) {
        if (nombre == null || nombre.isEmpty() || descripcion == null || descripcion.isEmpty()) {
            throw new IllegalArgumentException("El nombre y la descripción no pueden estar vacíos");
        }
        CategoriaIndustria categoria = new CategoriaIndustria(nombre, descripcion, regulaciones);
        return categoriaIndustriaRepository.save(categoria);
    }

    public Iterable<CategoriaDTO> listarCategorias() {
        List<CategoriaIndustria> categorias = categoriaIndustriaRepository.findAll();
        return categorias.stream()
                .map(categoria -> CategoriaDTO.builder()
                        .id(categoria.getId())
                        .nombre(categoria.getNombre())
                        .descripcion(categoria.getDescripcion())
                        .build())
                .toList();
    }

    public Iterable<CategoriaRegulacionDTO> listarTodasCategorias() {
        List<CategoriaIndustria> categorias = categoriaIndustriaRepository.findAll();
        return categorias.stream()
                .map(categoria -> CategoriaRegulacionDTO.builder()
                        .id(categoria.getId())
                        .nombre(categoria.getNombre())
                        .descripcion(categoria.getDescripcion())
                        .regulaciones(categoria.getRegulaciones())
                        .build())
                .toList();
    }
}
