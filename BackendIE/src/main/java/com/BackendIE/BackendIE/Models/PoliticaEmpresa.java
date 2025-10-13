package com.BackendIE.BackendIE.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name="PoliticasEmpresas")
@Getter
@Setter
@NoArgsConstructor
@Entity

public class PoliticaEmpresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long empresaid;

    @Column(length = 255)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    private Boolean aigenerada = false;

    @Column(length = 50)
    private String aimodeloversion;

    @Column(columnDefinition = "decimal(5,2)")
    private Double compliancescore;

    @Column(length = 20)
    private String estado = "draft";

    @Column(length = 20)
    private String version = "1.0";

    private Long aprobadopor;

    private LocalDateTime fechaaprobacion;

    @Column(updatable = false)
    private LocalDateTime createdat = LocalDateTime.now();

    private LocalDateTime updatedat = LocalDateTime.now();

    private LocalDateTime deletedat; // Soft delete

    public PoliticaEmpresa(Long empresaid, String titulo, String contenido, Boolean aigenerada, String aimodeloversion, Double compliancescore, String estado, String version, Long aprobadopor, LocalDateTime fechaaprobacion) {
        this.empresaid = empresaid;
        this.titulo = titulo;
        this.contenido = contenido;
        this.aigenerada = aigenerada;
        this.aimodeloversion = aimodeloversion;
        this.compliancescore = compliancescore;
        this.estado = estado;
        this.version = version;
        this.aprobadopor = aprobadopor;
        this.fechaaprobacion = fechaaprobacion;

    }
}

