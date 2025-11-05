package com.backendie.controller;

import com.backendie.dtos.*;
import com.backendie.models.Usuario;
import com.backendie.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/registerAdmin")
    public Usuario registerAdmin(@RequestBody RegisterAdmin registerAdmin){
        return usuarioService.registerAdmin(registerAdmin.getNombre(), registerAdmin.getEmail(), registerAdmin.getPassword());
    }

    @PostMapping("/registerUser")
    public Usuario registerUser(@RequestBody RegisterUser registerUser){
        return usuarioService.registerUser(registerUser.getEmpresaId(), registerUser.getNombre(), registerUser.getEmail(), registerUser.getPassword(), registerUser.getRol(), registerUser.getAdminId());
    }

    @PostMapping("/login")
    public Boolean login(@RequestBody Loginuserdto loginData){
        return usuarioService.login(loginData.getEmail(), loginData.getPassword());
    }

    @PostMapping("/request-register")
    public void requestRegister(@RequestBody RequestRegisterDTO dto){
        if ("admin".equalsIgnoreCase(dto.getTipo())) {
            usuarioService.requestRegisterAdmin(dto.getNombre(), dto.getEmail(), dto.getPassword());
        } else if ("user".equalsIgnoreCase(dto.getTipo())) {
            usuarioService.requestRegisterUser(dto.getEmpresaId(), dto.getNombre(), dto.getEmail(), dto.getPassword(), dto.getRol());
        } else {
            throw new IllegalArgumentException("Tipo de registro no soportado");
        }
    }

    @PostMapping("/verify-register")
    public void verifyRegister(@RequestParam("token") String token) {
        usuarioService.verifyRegister(token);
    }

    @PostMapping("/complete-register")
    public void completeRegister(@RequestParam("token") String token, @RequestParam("password") String password) {
        usuarioService.completeRegister(token, password);
    }

    @GetMapping("/adminsdto")
    public List<EmpresaDTO> getadminsDTO() {
        return usuarioService.getUsuariosAdminDTO();
    }

    @GetMapping("/usersdto")
    public List<EmpresaDTO> getusersempresaDTO(@RequestParam ("empresaId") Long empresaId) {
        return usuarioService.getUsuariosEmpresaDTO(empresaId);
    }

    @GetMapping("/users")
    public List<UserDTO> getusersDTO() {
        return usuarioService.getusersDTO();
    }

}
