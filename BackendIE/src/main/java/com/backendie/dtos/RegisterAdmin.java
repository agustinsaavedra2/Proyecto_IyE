package com.backendie.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterAdmin {
    private String nombre;
    private String email;
    private String password;
}
