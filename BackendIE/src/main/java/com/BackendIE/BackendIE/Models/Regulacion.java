package com.BackendIE.BackendIE.Models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name="Regulaciones")
@Getter
@Setter
@NoArgsConstructor
public class Regulacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="nombre")
    private String nombre;

    @Column(name="contenido")
    private String contenido;

    @Column(name="urlDocumento")
    private String urlDocumento;

    @Column(name="entidadEmisora")
    private String entidadEmisora;

    @Column(name="anioEmision")
    private Integer anioEmision;

    @Column(name="createdAt")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name="deletedAt")
    private LocalDateTime deletedAt; // Soft delete

    public Regulacion(String nombre, String contenido, String urlDocumento, String entidadEmisora, Integer anioEmision) {
        this.nombre = nombre;
        this.contenido = contenido;
        this.urlDocumento = urlDocumento;
        this.entidadEmisora = entidadEmisora;
        this.anioEmision = anioEmision;

    }
}
