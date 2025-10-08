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
@Table(name = "politicastemplates")
public class PoliticaTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long frameworktemplateid;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenidobase;

    @Column(nullable = false, length = 50)
    private String tipo; // policy, procedure, standard, guideline

    @Column(columnDefinition = "TEXT")
    private String variablespersonalizacion;

    @Column(length = 20)
    private String version = "1.0";

    @Column(updatable = false)
    private LocalDateTime createdat = LocalDateTime.now();

    private LocalDateTime updatedat = LocalDateTime.now();

    private LocalDateTime deletedat; // Soft delete

    public PoliticaTemplate (Long frameworktemplateid, String titulo, String contenidobase, String tipo, String variablespersonalizacion, String version) {
        this.frameworktemplateid = frameworktemplateid;
        this.titulo = titulo;
        this.contenidobase = contenidobase;
        this.tipo = tipo;
        this.variablespersonalizacion = variablespersonalizacion;
        this.version = version;
    }

}

