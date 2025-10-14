package com.BackendIE.BackendIE.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name="suscripciones")
@Getter
@Setter
@NoArgsConstructor
public class Suscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresaId")
    private Long empresaId;

    @Column(name="plan" )
    private String plan;

    @Column(name = "precioMensual")
    private Double precioMensual = 0.0;

    @Column(name = "estado")
    private String estado; // activo, cancelado, pendiente

    @Column(name="maxUsuarios")
    private Integer maxUsuarios;

    @Column(name="fechaInicio")
    private LocalDateTime fechaInicio;

    @Column(name="fechaFin")
    private LocalDateTime fechaFin;

    @Column(name="createdAt")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name="deletedAt")
    private LocalDateTime deletedAt; // Soft delete

    public Suscripcion(Long empresaId, String plan, String estado, Integer maxUsuarios, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        this.empresaId = empresaId;
        this.plan = plan;
        this.estado = estado;
        this.maxUsuarios = maxUsuarios;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }


}
