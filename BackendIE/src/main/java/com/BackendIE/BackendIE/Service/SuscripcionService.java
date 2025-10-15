package com.BackendIE.BackendIE.Service;

import com.BackendIE.BackendIE.Models.Empresa;
import com.BackendIE.BackendIE.Models.Plan;
import com.BackendIE.BackendIE.Models.Suscripcion;
import com.BackendIE.BackendIE.Models.Usuario;
import com.BackendIE.BackendIE.Repository.EmpresaRepository;
import com.BackendIE.BackendIE.Repository.PlanRepository;
import com.BackendIE.BackendIE.Repository.SuscripcionRepository;
import com.BackendIE.BackendIE.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SuscripcionService {

    @Autowired
    private SuscripcionRepository suscripcionRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

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

        return suscripcionRepository.save(suscripcion);
    }
}