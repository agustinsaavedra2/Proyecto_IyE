package com.backendie.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="categoriaIndustrias")
@Data
@NoArgsConstructor
public class CategoriaIndustria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="nombre")
    private String nombre;

    @Column(name="descripcion")
    private String descripcion;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="CategoriaRegulaciones", joinColumns=@JoinColumn(name="categoriaId"))
    @Column(name="regulaciones")
    private List<String> regulaciones;

    @Column(name="createdAt")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name="deletedAt")
    private LocalDateTime deletedAt; // Soft delete

    public CategoriaIndustria(String nombre, String descripcion, List<String> regulaciones) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.regulaciones = regulaciones;
    }
}
