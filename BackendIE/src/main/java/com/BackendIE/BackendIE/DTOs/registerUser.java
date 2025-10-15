package com.BackendIE.BackendIE.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class registerUser {
    private Long empresaId;
    private String nombre;
    private String email;
    private String password;
    private String rol; // admin, complianceofficer, auditor, viewer

}
