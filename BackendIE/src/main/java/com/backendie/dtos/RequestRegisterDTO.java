package com.backendie.dtos;

import lombok.Data;

@Data
public class RequestRegisterDTO {
    private String tipo;
    private Long empresaId;
    private String nombre;
    private String email;
    private String password;
    private String rol;
    private Long adminId;
}

