package com.BackendIE.BackendIE.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="planes")
@Getter
@Setter
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
