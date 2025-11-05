package com.backendie.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoriaRegulacionDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private List<String> regulaciones;
}
