package com.backendie.service;

import com.backendie.models.Auditoria;
import com.backendie.repository.AuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

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
