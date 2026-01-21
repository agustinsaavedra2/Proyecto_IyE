package com.backendie.models;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.Filter;

@Filter(name = "categoriaFilter", condition = "empresa_id IN (select id from empresas where categoria_id = :categoriaId)")
@Table(name="usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
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

    private Boolean activo;

    private LocalDateTime ultimoAcceso;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createDat = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updateDat = LocalDateTime.now();

    private LocalDateTime deleteDat; // Soft delete

    // New: token version for immediate JWT invalidation (increment to invalidate existing tokens)
    @Column(name = "token_version", nullable = false)
    @Builder.Default
    private Integer tokenVersion = 0;
}