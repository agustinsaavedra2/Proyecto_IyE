package com.BackendIE.BackendIE.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "politicasempresas")
public class PoliticaEmpresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long empresaid;

    @Column(nullable = false)
    private Long politicatemplateid;

    @Column(length = 255)
    private String titulopersonalizado;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenidopersonalizado;

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
    // Getters y setters
}

