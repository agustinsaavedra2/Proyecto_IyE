package com.backendie.dtos;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private Long empresaId;
    private String nombre;
    private String email;
    private String rol;
    private Boolean activo;
}
