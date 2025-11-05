package com.backendie.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarEmpresa {
    public Long admin;
    public Long categoriaId;
    public String nombre;
    public String codigoEmpresa;
    public String ubicacion;
    public String descripcion;
}
