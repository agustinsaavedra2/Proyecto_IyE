package com.BackendIE.BackendIE.Service;

import com.BackendIE.BackendIE.Models.Empresa;
import com.BackendIE.BackendIE.Models.Usuario;
import com.BackendIE.BackendIE.Repository.EmpresaRepository;
import com.BackendIE.BackendIE.Repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

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

}
