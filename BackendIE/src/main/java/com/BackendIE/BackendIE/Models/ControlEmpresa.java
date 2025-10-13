package com.BackendIE.BackendIE.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name="ControlEmpresas")
@Getter
@Setter
@NoArgsConstructor
public class ControlEmpresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresaId")
    private Long empresaId;

    @Column(name="codigoControl" )
    private String codigoControl;

    @Column(name="nombre" )
    private String nombre;

    @Column(name="descripcion" )
    private String descripcion;

    @Column(name="criticidad")
    private String criticidad; // bajo, medio, alto

    @Column(name="frecuenciaRevision")
    private String frecuenciaRevision; // diaria, semanal, mensual, trimestral, anual

    @Column(name="evidenciaRequerida")
    private String evidenciaRequerida; // descripcion de la evidencia

    @Column(name="responsable")
    private Long responsable; // persona o rol responsable del control

    @Column(name="estado")
    private String estado; // activo, inactivo, en progreso

    @Column(name="fechaRevision")
    private LocalDateTime fechaRevision; // ultima fecha de revision

    @Column(name="createdAt")
    private LocalDateTime createdAt = LocalDateTime.now(); // fecha de creacion

    @Column(name="updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now(); // fecha de ultima actualizacion

    @Column(name="deletedAt")
    private LocalDateTime deletedAt; // Soft delete

    public ControlEmpresa(Long empresaId, String codigoControl, String nombre, String descripcion, String criticidad, String frecuenciaRevision, String evidenciaRequerida, Long responsable, String estado, LocalDateTime fechaRevision) {
        this.empresaId = empresaId;
        this.codigoControl = codigoControl;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.criticidad = criticidad;
        this.frecuenciaRevision = frecuenciaRevision;
        this.evidenciaRequerida = evidenciaRequerida;
        this.responsable = responsable;
        this.estado = estado;
        this.fechaRevision = fechaRevision;

    }

}
