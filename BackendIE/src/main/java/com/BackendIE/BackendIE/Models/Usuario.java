package com.BackendIE.BackendIE.Models;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name="usuarios")
@Entity
@Getter
@Setter
@NoArgsConstructor

public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresaId")
    private Long empresaId;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String rol;  // admin, complianceofficer, auditor, viewer

    private Boolean activo = true;

    private LocalDateTime ultimoAcceso;

    @Column(updatable = false)
    private LocalDateTime createDat = LocalDateTime.now();

    private LocalDateTime updateDat = LocalDateTime.now();

    private LocalDateTime deleteDat; // Soft delete

    public Usuario(Long empresaId, String nombre, String email, String passwordHash, String rol) {
        this.empresaId = empresaId;
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;

    }
}
