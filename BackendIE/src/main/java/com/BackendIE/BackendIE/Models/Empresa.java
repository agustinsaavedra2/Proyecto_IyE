package com.BackendIE.BackendIE.Models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Table(name="Empresas")
@Getter
@Setter
@NoArgsConstructor
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "categoriaId")
    private Long categoriaId;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "codigoEmpresa", unique = true)
    private String codigoEmpresa;

    @Column(name = "numEmpleados")
    private Integer numEmpleados;

    @Column(name="empleados")
    private List<Long> empleados;

    @Column(name="ubicacion" )
    private String ubicacion;

    @Column(name = "descripcion" )
    private String descripcion;

    @Column(name="status")
    private String status;

    @Column(name = "createdAt")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "deletedAt")
    private LocalDateTime deletedAt; // Soft delete

    public Empresa(Long categoriaId, String nombre, String codigoEmpresa, Integer numEmpleados, String ubicacion, String descripcion, String status) {
        this.categoriaId = categoriaId;
        this.nombre = nombre;
        this.codigoEmpresa = codigoEmpresa;
        this.numEmpleados = numEmpleados;
        this.ubicacion = ubicacion;
        this.descripcion = descripcion;
        this.status = status;
    }

}
