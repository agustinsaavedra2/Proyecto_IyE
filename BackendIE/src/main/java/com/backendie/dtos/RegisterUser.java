package com.backendie.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUser {
    private Long empresaId;
    private String nombre;
    private String email;
    private String password;
    private String rol; // admin, complianceofficer, auditor, viewer
    private Long adminId;

}
