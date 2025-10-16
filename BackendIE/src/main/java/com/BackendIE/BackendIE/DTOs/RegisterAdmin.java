package com.BackendIE.BackendIE.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterAdmin {
    private String nombre;
    private String email;
    private String password;
}
