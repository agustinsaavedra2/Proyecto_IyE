package com.BackendIE.BackendIE.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name="Auditorias")
@Getter
@Setter
@NoArgsConstructor
public class Auditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="empresaId")
    private Long empresaId;

    @Column(name="tipo")
    private String tipo; // interno, externo, regulatorio

    @Column(name="objetivo")
    private String objetivo;

    @Column(name="alcance")
    private String alcance;

    @Column(name="auditorLider")
    private String auditorLider;

    @Column(name="estado")
    private String estado; // planificado, en progreso, completado, cerrado

    @Column(name="fechaInicio")
    private LocalDateTime fechaInicio = LocalDateTime.now();

    @Column(name="fechaFin")
    private LocalDateTime fechaFin;

    @Column(name="score")
    private Double score; // 0.0 - 100.0

    @Column(name="hallazgosCriticos")
    private Integer hallazgosCriticos;

    @Column(name="hallazgosCriticosMsj")
    private String hallazgosCriticosMsj;

    @Column(name="hallazgosMayores")
    private Integer hallazgosMayores;

    @Column(name="hallazgosMayoresMsj")
    private String hallazgosMayoresMsj;

    @Column(name="hallazgosMenores")
    private Integer hallazgosMenores;

    @Column(name="hallazgosMenoresMsj")
    private String hallazgosMenoresMsj;

    @Column(name="recomendaciones")
    private String recomendaciones;

    @Column(name="createdAt")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name="deletedAt")
    private LocalDateTime deletedAt; // Soft delete

    public Auditoria(Long empresaId, String tipo, String objetivo, String alcance, String auditorLider, String estado, LocalDateTime fechaInicio, LocalDateTime fechaFin, Double score, Integer hallazgosCriticos, String hallazgosCriticosMsj, Integer hallazgosMayores, String hallazgosMayoresMsj, Integer hallazgosMenores, String hallazgosMenoresMsj, String recomendaciones) {
        this.empresaId = empresaId;
        this.tipo = tipo;
        this.objetivo = objetivo;
        this.alcance = alcance;
        this.auditorLider = auditorLider;
        this.estado = estado;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.score = score;
        this.hallazgosCriticos = hallazgosCriticos;
        this.hallazgosCriticosMsj = hallazgosCriticosMsj;
        this.hallazgosMayores = hallazgosMayores;
        this.hallazgosMayoresMsj = hallazgosMayoresMsj;
        this.hallazgosMenores = hallazgosMenores;
        this.hallazgosMenoresMsj = hallazgosMenoresMsj;
        this.recomendaciones = recomendaciones;

    }
}
