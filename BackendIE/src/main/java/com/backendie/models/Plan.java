package com.backendie.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="planes")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private Double precio;

    private Integer maxUsuarios;

    private Integer duracionMeses; // Duración en meses

    private Integer maxConsultasMensuales;

    private Boolean unlimited;

    public Plan(String nombre, Double precio, Integer maxUsuarios, Integer duracionMeses, Integer maxConsultasMensuales, Boolean unlimited) {
        this.nombre = nombre;
        this.precio = precio;
        this.maxUsuarios = maxUsuarios;
        this.duracionMeses = duracionMeses;
        this.maxConsultasMensuales = maxConsultasMensuales;
        this.unlimited = unlimited;

    }
}
