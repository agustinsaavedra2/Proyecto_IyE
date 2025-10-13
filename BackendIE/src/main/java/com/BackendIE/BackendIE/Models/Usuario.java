package com.BackendIE.BackendIE.Models;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name="Usuarios")
@Entity
@Getter
@Setter
@NoArgsConstructor

public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresaId", nullable = false)
    private Long empresaId;  // FK a empresas en esquema globalmanagement

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordhash;

    @Column(nullable = false, length = 50)
    private String rol;  // admin, complianceofficer, auditor, viewer

    @Column(length = 100)
    private String departamento;

    private Boolean activo = true;

    private LocalDateTime ultimoacceso;

    @Column(columnDefinition = "jsonb")
    private String configuracionnotificaciones;

    @Column(updatable = false)
    private LocalDateTime createdat = LocalDateTime.now();

    private LocalDateTime updatedat = LocalDateTime.now();

    private LocalDateTime deletedat; // Soft delete

    public Usuario(Long empresaId, String nombre, String email, String passwordhash, String rol, String departamento, String configuracionnotificaciones) {
        this.empresaId = empresaId;
        this.nombre = nombre;
        this.email = email;
        this.passwordhash = passwordhash;
        this.rol = rol;
        this.departamento = departamento;
        this.configuracionnotificaciones = configuracionnotificaciones;
    }
}
