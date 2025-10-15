package com.BackendIE.BackendIE.Models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Table(name="empresas")
@Data
@NoArgsConstructor
@Entity
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

    @ElementCollection
    @CollectionTable(name="EmpresaEmpleados", joinColumns=@JoinColumn(name="empresaId"))
    @Column(name="empleados")
    private List<Long> empleados;

    @Column(name="ubicacion" )
    private String ubicacion;

    @Column(name = "descripcion" )
    private String descripcion;

    @Column(name="status")
    private String status = "inactive"; // active, inactive, pending

    @Column(name = "createdAt")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "deletedAt")
    private LocalDateTime deletedAt; // Soft delete

    public Empresa(Long categoriaId, String nombre, String codigoEmpresa, List<Long> empleados, String ubicacion, String descripcion) {
        this.categoriaId = categoriaId;
        this.nombre = nombre;
        this.codigoEmpresa = codigoEmpresa;
        this.empleados = empleados;
        this.ubicacion = ubicacion;
        this.descripcion = descripcion;
    }

}
