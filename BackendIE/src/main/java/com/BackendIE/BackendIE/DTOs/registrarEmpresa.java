package com.BackendIE.BackendIE.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class registrarEmpresa {
    public Long admin;
    public Long categoriaId;
    public String nombre;
    public String codigoEmpresa;
    public String ubicacion;
    public String descripcion;
}
