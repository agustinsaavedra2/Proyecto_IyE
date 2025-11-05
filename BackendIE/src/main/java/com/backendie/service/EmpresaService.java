package com.backendie.service;

import com.backendie.dtos.EmpresaDTO;
import com.backendie.models.Empresa;
import com.backendie.models.Usuario;
import com.backendie.repository.EmpresaRepository;
import com.backendie.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    private final UsuarioRepository usuarioRepository;

    @Transactional
    public Empresa registrarEmpresa(Long admin, Long categoriaId, String nombre, String codigoEmpresa, String ubicacion, String descripcion){
        if (admin == null || categoriaId == null || nombre.isEmpty() || ubicacion.isEmpty()) {
            throw new IllegalArgumentException("All fields are required");
        }
        Usuario amindValid = usuarioRepository.findById(admin).orElse(null);
        if (amindValid == null || !amindValid.getRol().equals("admin")) {
            throw new IllegalArgumentException("Admin user not found or not an admin");
        }
        List<Long> empleados = new ArrayList<>();
        empleados.add(admin);
        Empresa empresa = new Empresa(categoriaId, nombre, codigoEmpresa, empleados, ubicacion, descripcion);
        empresaRepository.save(empresa);
        amindValid.setEmpresaId(empresa.getId());
        usuarioRepository.save(amindValid);
        return empresa;
    }

    public Empresa validarEmpresaYUsuario(Long empresaId, Long usuarioId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        if (!usuario.getEmpresaId().equals(empresaId))
            throw new IllegalArgumentException("Usuario no pertenece a la empresa.");
        if (!"active".equals(empresa.getStatus()))
            throw new IllegalArgumentException("La empresa debe estar activa.");

        return empresa;
    }

    public List<EmpresaDTO> obtenerEmpresasDTO(){
        List<EmpresaDTO> resumen = new ArrayList<>();
        for (Empresa e : empresaRepository.findAll()) {
            resumen.add(new EmpresaDTO(e.getId(), e.getNombre()));
        }
        return resumen;
    }

}
