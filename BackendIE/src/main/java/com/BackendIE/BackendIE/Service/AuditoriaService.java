package com.BackendIE.BackendIE.Service;

import com.BackendIE.BackendIE.Models.Auditoria;
import com.BackendIE.BackendIE.Repository.AuditoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuditoriaService {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    // Crear o actualizar
    public Auditoria save(Auditoria auditoria) {
        return auditoriaRepository.save(auditoria);
    }

    // Obtener todas las auditorías
    public List<Auditoria> findAll() {
        return auditoriaRepository.findAll();
    }

    // Buscar por ID
    public Optional<Auditoria> findById(String id) {
        return auditoriaRepository.findById(id);
    }
}
