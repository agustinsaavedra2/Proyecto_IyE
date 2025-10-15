package com.BackendIE.BackendIE.Service;

import com.BackendIE.BackendIE.Models.Empresa;
import com.BackendIE.BackendIE.Models.Usuario;
import com.BackendIE.BackendIE.Repository.EmpresaRepository;
import com.BackendIE.BackendIE.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;


    public Usuario registerAdmin(String nombre, String email, String passwordHash){
        if(nombre.isEmpty() || email.isEmpty() || passwordHash.isEmpty()){
            throw new IllegalArgumentException("All fields are required");
        }
        if (usuarioRepository.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email already in use");
        }
        Usuario usuario = new Usuario(-1L, nombre, email, passwordHash, "admin");
        return usuarioRepository.save(usuario);
    }

    public Usuario registerUser(Long empresaId, String nombre, String email, String passwordHash, String rol, Long adminId){
        if(empresaId == null || nombre.isEmpty() || email.isEmpty() || passwordHash.isEmpty() || rol.isEmpty() || adminId == null){
            throw new IllegalArgumentException("All fields are required");
        }
        Usuario adminValid = usuarioRepository.findById(adminId).orElse(null);
        if(adminValid == null || !adminValid.getRol().equals("admin")){
            throw new IllegalArgumentException("Admin user not found or not an admin");
        }
        if (usuarioRepository.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email already in use");
        }
        if(!rol.equals("admin") && !rol.equals("complianceofficer") && !rol.equals("auditor") && !rol.equals("viewer")){
            throw new IllegalArgumentException("Invalid role");
        }
        Usuario usuario = new Usuario(empresaId, nombre, email, passwordHash, rol);
        return usuarioRepository.save(usuario);
    }

    public boolean login(String email, String password){
        Usuario usuario = usuarioRepository.findByEmail(email);
        if(usuario == null){
            return false;
        }
        if(!usuario.getPasswordHash().equals(password)){
            return false;
        }
        return true;
    }



}
