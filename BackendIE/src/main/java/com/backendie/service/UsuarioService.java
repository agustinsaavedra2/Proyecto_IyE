package com.backendie.service;

import com.backendie.dtos.EmpresaDTO;
import com.backendie.dtos.UserDTO;
import com.backendie.models.Empresa;
import com.backendie.models.Usuario;
import com.backendie.repository.EmpresaRepository;
import com.backendie.repository.UsuarioRepository;
import com.backendie.security.TokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    private final EmpresaRepository empresaRepository;

    private final EmailService emailService;

    private final TokenService tokenService;

    private static final String ADMIN_ROLE = "admin";

    private final Logger log = LoggerFactory.getLogger(UsuarioService.class);


    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Usuario registerAdmin(String nombre, String email, String passwordHash) {
        if (nombre.isEmpty() || email.isEmpty() || passwordHash.isEmpty()) {
            throw new IllegalArgumentException("All fields are required");
        }
        if (usuarioRepository.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email already in use");
        }
        String hashed = passwordEncoder.encode(passwordHash);
        Usuario usuario = Usuario.builder()
            .nombre(nombre)
            .email(email)
            .passwordHash(hashed)
            .rol(ADMIN_ROLE)
            .tokenVersion(0)
            .build();
        return usuarioRepository.save(usuario);
    }

    public List<EmpresaDTO> getUsuariosAdminDTO() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarios.stream()
                .filter(u ->ADMIN_ROLE.equals(u.getRol()))
                .map(usuario -> EmpresaDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .build()).toList();
    }

    public List<EmpresaDTO> getUsuariosEmpresaDTO(Long empresaId) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarios.stream()
                .filter(u -> empresaId.equals(u.getEmpresaId()))
                .map(usuario -> EmpresaDTO.builder()
                        .id(usuario.getId())
                        .nombre(usuario.getNombre())
                        .build()).toList();
    }

    @Transactional
    public Usuario registerUser(Long empresaId, String nombre, String email, String passwordHash, String rol, Long adminId) {
        if (empresaId == null || nombre.isEmpty() || email.isEmpty() || passwordHash.isEmpty() || rol.isEmpty() || adminId == null) {
            throw new IllegalArgumentException("All fields are required");
        }

        Usuario adminValid = usuarioRepository.findById(adminId).orElse(null);
        if (adminValid == null || !ADMIN_ROLE.equals(adminValid.getRol())) {
            throw new IllegalArgumentException("Admin user not found or not an admin");
        }
        if (usuarioRepository.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (!rol.equals(ADMIN_ROLE) && !rol.equals("complianceofficer") && !rol.equals("auditor") && !rol.equals("viewer")) {
            throw new IllegalArgumentException("Invalid role");
        }
        Empresa empresa = empresaRepository.findById(empresaId).orElse(null);
        if (empresa == null) {
            throw new IllegalArgumentException("Empresa not found");
        }
        String hashed = passwordEncoder.encode(passwordHash);
        Usuario usuario = Usuario.builder()
                .empresaId(empresaId)
                .nombre(nombre)
                .email(email)
                .passwordHash(hashed)
                .rol(rol)
                .tokenVersion(0)
                .build();
        usuarioRepository.save(usuario);
        empresa.getEmpleados().add(usuario.getId());
        empresaRepository.save(empresa);
        return usuario;
    }

    public void requestRegisterAdmin(String nombre, String email, String password) {
        if (nombre == null || email == null || password == null) throw new IllegalArgumentException("Datos requeridos");
        Usuario existente = usuarioRepository.findByEmail(email);
        if (existente != null) {
            return;
        }
        log.debug("Usuario existente check returned: {}", existente);
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuario.setRol(ADMIN_ROLE);
        usuario.setActivo(false);
        usuario.setTokenVersion(0);
        usuarioRepository.save(usuario);
        String token = tokenService.generateEmailVerificationToken(usuario.getId());
        log.info("Se genero el token: {}", token);
        try {
            emailService.sendVerificationEmail(email, token);
        } catch (Exception e) {
            log.error("Error intentando enviar correo de verificación para {}: {}", email, e.toString());
        }
    }

    public void requestRegisterUser(Long empresaId, String nombre, String email, String password, String rol) {
        if (empresaId == null || nombre == null || email == null || password == null || rol == null) throw new IllegalArgumentException("Datos requeridos");
        Usuario existente = usuarioRepository.findByEmail(email);
        if (existente != null) {
            return;
        }
        Usuario usuario = Usuario.builder()
                .empresaId(empresaId)
                .nombre(nombre)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .activo(false)
                .tokenVersion(0)
                .build();
        usuarioRepository.save(usuario);
        String token = tokenService.generateEmailVerificationToken(usuario.getId());
        try {
            emailService.sendVerificationEmail(email, token);
        } catch (Exception e) {
            log.error("Error intentando enviar correo de verificación para {}: {}", email, e.toString());
        }
    }

    public void verifyRegister(String token) {
        Long userId = tokenService.validateAndExtractUserId(token);
        Usuario usuario = usuarioRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        usuarioRepository.save(usuario);
    }

    public void completeRegister(String token, String password) {
        Long userId = tokenService.validateAndExtractUserId(token);
        Usuario usuario = usuarioRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        usuario.setPasswordHash(password);
        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }

    public Usuario authenticate(String email, String password) {
        if (email == null || password == null) throw new IllegalArgumentException("Datos requeridos");
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario != null && passwordEncoder.matches(password, usuario.getPasswordHash())) {
            usuario.setUltimoAcceso(LocalDateTime.now());
            usuarioRepository.save(usuario);
            return usuario;
        }
        return null;
    }

    public boolean login(String email, String password) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        return usuario != null && passwordEncoder.matches(password, usuario.getPasswordHash());
    }

    public List<UserDTO> getusersDTO() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarios.stream()
                .map(usuario -> UserDTO.builder()
                        .id(usuario.getId())
                        .empresaId(usuario.getEmpresaId())
                        .nombre(usuario.getNombre())
                        .email(usuario.getEmail())
                        .rol(usuario.getRol())
                        .activo(usuario.getActivo())
                        .build()).toList();
    }

    public List<EmpresaDTO> getusersRolDTO(Long empresaId, String rol) {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarios.stream()
                .filter(u -> empresaId.equals(u.getEmpresaId()) && rol.equals(u.getRol()))
                .map(usuario -> EmpresaDTO.builder()
                        .id(usuario.getId())
                        .nombre(usuario.getNombre())
                        .build()).toList();
    }

    public Long getCategoriaIdByEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario != null) {
            return empresaRepository.findById(usuario.getEmpresaId()).map(
                    e -> e.getCategoriaId())
                    .orElse(null);
        } else return null;
    }

    public Usuario getById(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }
}
