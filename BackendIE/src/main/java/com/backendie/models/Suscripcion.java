package com.backendie.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="suscripciones")
@Data
@NoArgsConstructor
public class Suscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresaId")
    private Long empresaId;

    @Column(name="plan" )
    private String plan;

    @Column(name = "precio")
    private Double precio = 0.0;

    @Column(name = "estado")
    private Boolean estado = true;

    @Column(name="maxUsuarios")
    private Integer maxUsuarios;

    @Column(name="fechaInicio")
    private LocalDateTime fechaInicio = LocalDateTime.now();

    @Column(name="fechaFin")
    private LocalDateTime fechaFin;

    @Column(name="createdAt")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name="deletedAt")
    private LocalDateTime deletedAt; // Soft delete

    public Suscripcion(Long empresaId, String plan) {
        this.empresaId = empresaId;
        this.plan = plan;
    }


}
