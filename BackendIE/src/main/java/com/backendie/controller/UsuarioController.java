package com.backendie.controller;

import com.backendie.dtos.*;
import com.backendie.models.Usuario;
import com.backendie.security.JwtUtil;
import com.backendie.security.RefreshTokenService;
import com.backendie.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/registerAdmin")
    public Usuario registerAdmin(@RequestBody RegisterAdmin registerAdmin){
        return usuarioService.registerAdmin(registerAdmin.getNombre(), registerAdmin.getEmail(), registerAdmin.getPassword());
    }

    @PostMapping("/registerUser")
    public Usuario registerUser(@RequestBody RegisterUser registerUser){
        return usuarioService.registerUser(registerUser.getEmpresaId(), registerUser.getNombre(), registerUser.getEmail(), registerUser.getPassword(), registerUser.getRol(), registerUser.getAdminId());
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody Loginuserdto loginData){
        Usuario usuario = usuarioService.authenticate(loginData.getEmail(), loginData.getPassword());
        if (usuario == null) {
            return ResponseEntity.status(401).build();
        }
        List<String> roles = usuario.getRol() != null ? List.of(usuario.getRol()) : List.of();
        int tokenVersion = usuario.getTokenVersion() == null ? 0 : usuario.getTokenVersion();
        String token = jwtUtil.generateToken(usuario.getEmail(), roles, tokenVersion);
        String refresh = refreshTokenService.createRefreshToken(usuario.getId());
        long expiresSeconds = jwtUtil.getExpirationSeconds();
        JwtResponse resp = JwtResponse.builder().token(token).refreshToken(refresh).expiresIn(expiresSeconds).build();
        return ResponseEntity.ok(resp);
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

    @GetMapping("/usersRol")
    public List<EmpresaDTO> getusersRolDTO(@RequestParam ("empresaId") Long empresaId, @RequestParam ("rol") String rol) {
        return usuarioService.getusersRolDTO(empresaId, rol);
    }

    @GetMapping("/users")
    public List<UserDTO> getusersDTO() {
        return usuarioService.getusersDTO();
    }

}
