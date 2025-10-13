package com.BackendIE.BackendIE.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Table(name="CategoriaIndustrias")
@Setter
@Getter
@NoArgsConstructor
public class CategoriaIndustria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="nombre")
    private String nombre;

    @Column(name="descripcion")
    private String descripcion;

    @Column(name="regulaciones")
    private List<Long> regulaciones;

    @Column(name="createdAt")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name="deletedAt")
    private LocalDateTime deletedAt; // Soft delete

    public CategoriaIndustria(String nombre, String descripcion, List<Long> regulaciones) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.regulaciones = regulaciones;
    }
}
