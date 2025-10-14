package com.BackendIE.BackendIE.Models;

import jakarta.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table(name="politicasEmpresas")
@Data
@NoArgsConstructor
@Entity
public class PoliticaEmpresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long empresaId;

    @Column(length = 255)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    private Boolean aiGenerada = false;

    @Column(length = 50)
    private String aiModeloVersion;

    @Column(columnDefinition = "decimal(5,2)")
    private Double complianceScore;

    @Column(length = 20)
    private String estado = "draft";

    @Column(length = 20)
    private String version = "1.0";

    private Long aprobadoPor; // ID del usuario que aprobó

    private LocalDateTime fechaAprobacion;

    @Column(updatable = false)
    private LocalDateTime createDat = LocalDateTime.now();

    private LocalDateTime updateDat = LocalDateTime.now();

    private LocalDateTime deleteDat; // Soft delete
}

