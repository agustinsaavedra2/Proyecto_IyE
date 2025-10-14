package com.BackendIE.BackendIE.Service;

import com.BackendIE.BackendIE.Models.Regulacion;
import com.BackendIE.BackendIE.Repository.RegulacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RegulacionService {

    @Autowired
    private RegulacionRepository regulacionRepository;

    // Crear o actualizar
    public Regulacion save(Regulacion regulacion) {
        // Si quieres manejar createdAt y updatedAt, agregamos campos temporales
        // Si no los agregas al modelo, esto lo puedes omitir
        return regulacionRepository.save(regulacion);
    }

    // Listar todas
    public List<Regulacion> findAll() {
        return regulacionRepository.findAll();
    }

    // Buscar por ID
    public Optional<Regulacion> findById(String id) {
        return regulacionRepository.findById(id);
    }

    // Actualizar
    public Regulacion update(String id, Regulacion updated) {
        return regulacionRepository.findById(id)
                .map(existing -> {
                    existing.setNombre(updated.getNombre());
                    existing.setContenido(updated.getContenido());
                    existing.setUrlDocumento(updated.getUrlDocumento());
                    existing.setEntidadEmisora(updated.getEntidadEmisora());
                    existing.setAnioEmision(updated.getAnioEmision());
                    return regulacionRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("No se encontró la regulación con id " + id));
    }

    // Eliminar (hard delete)
    public void delete(String id) {
        regulacionRepository.deleteById(id);
    }
}
