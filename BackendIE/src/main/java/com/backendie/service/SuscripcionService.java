package com.backendie.service;

import com.backendie.models.Empresa;
import com.backendie.models.Plan;
import com.backendie.models.Suscripcion;
import com.backendie.models.Usuario;
import com.backendie.repository.EmpresaRepository;
import com.backendie.repository.PlanRepository;
import com.backendie.repository.SuscripcionRepository;
import com.backendie.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SuscripcionService {

    private final SuscripcionRepository suscripcionRepository;

    private final EmpresaRepository empresaRepository;

    private final PlanRepository planRepository;

    private final UsuarioRepository usuarioRepository;

    public Suscripcion suscribirse(Long empresaId, String plan, Long adminId){
        if(empresaId == null || plan.isEmpty()){
            throw new IllegalArgumentException("All fields are required");
        }
        Empresa empresa = empresaRepository.findById(empresaId).orElse(null);
        Plan planValid = planRepository.findByNombre(plan);
        Usuario adminValid = usuarioRepository.findById(adminId).orElse(null);
        if(empresa == null || planValid == null){
            throw new IllegalArgumentException("Empresa not found or Plan not found");
        }
        if(adminValid == null || !adminValid.getRol().equals("admin") || !adminValid.getEmpresaId().equals(empresaId)){
            throw new IllegalArgumentException("Admin user not found or not an admin");
        }
        Suscripcion suscripcion = new Suscripcion(empresaId, plan);
        suscripcion.setPrecio(planValid.getPrecio());
        suscripcion.setMaxUsuarios(planValid.getMaxUsuarios());
        suscripcion.setFechaFin(suscripcion.getFechaInicio().plusMonths(planValid.getDuracionMeses()));

        empresa.setStatus("active");
        empresaRepository.save(empresa);

        return suscripcionRepository.save(suscripcion);
    }
}