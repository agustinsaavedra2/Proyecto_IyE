package com.BackendIE.BackendIE.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name="Riesgos")
@Getter
@Setter
@NoArgsConstructor
public class Riesgo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="empresaId")
    private Long empresaId;

    @Column(name="titulo")
    private String titulo;

    @Column(name="descripcion")
    private String descripcion;

    @Column(name="categoria")
    private String categoria; // operativo, financiero, estratégico, cumplimiento

    @Column(name="probabilidad")
    private String probabilidad; // baja, media, alta

    @Column(name="impacto")
    private String impacto; // bajo, medio, alto

    @Column(name="nivelRiesgo")
    private String nivelRiesgo; // bajo, medio, alto

    @Column(name="medidasMitigacion")
    private String medidasMitigacion;

    @Column(name="responsable")
    private Long responsable;

    @Column(name="estado")
    private String estado; // abierto, en progreso, mitigado, cerrado

    @Column(name="createdAt")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name="deletedAt")
    private LocalDateTime deletedAt;

    public Riesgo(Long empresaId, String titulo, String descripcion, String categoria, String probabilidad, String impacto, String nivelRiesgo, String medidasMitigacion, Long responsable, String estado) {
        this.empresaId = empresaId;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.probabilidad = probabilidad;
        this.impacto = impacto;
        this.nivelRiesgo = nivelRiesgo;
        this.medidasMitigacion = medidasMitigacion;
        this.responsable = responsable;
        this.estado = estado;

    }
}
