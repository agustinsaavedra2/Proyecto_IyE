package com.backendie.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="planes")
@Data
@NoArgsConstructor
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private Double precio;

    private Integer maxUsuarios;

    private Integer duracionMeses; // Duración en meses

    public Plan(String nombre, Double precio, Integer maxUsuarios, Integer duracionMeses) {
        this.nombre = nombre;
        this.precio = precio;
        this.maxUsuarios = maxUsuarios;
        this.duracionMeses = duracionMeses;
    }
}
