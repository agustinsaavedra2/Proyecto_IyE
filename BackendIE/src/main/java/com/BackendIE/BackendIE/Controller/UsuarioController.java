package com.BackendIE.BackendIE.Controller;

import com.BackendIE.BackendIE.DTOs.RegisterAdmin;
import com.BackendIE.BackendIE.DTOs.RegisterUser;
import com.BackendIE.BackendIE.Models.Usuario;
import com.BackendIE.BackendIE.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/registerAdmin")
    public Usuario registerAdmin(@RequestBody RegisterAdmin registerAdmin){
        return usuarioService.registerAdmin(registerAdmin.getNombre(), registerAdmin.getEmail(), registerAdmin.getPassword());
    }

    @PostMapping("/registerUser")
    public Usuario registerUser(@RequestBody RegisterUser registerUser){
        return usuarioService.registerUser(registerUser.getEmpresaId(), registerUser.getNombre(), registerUser.getEmail(), registerUser.getPassword(), registerUser.getRol(), registerUser.getAdminId());
    }
}
