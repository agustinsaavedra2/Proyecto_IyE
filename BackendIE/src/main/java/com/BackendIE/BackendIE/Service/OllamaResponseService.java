package com.BackendIE.BackendIE.Service;

import com.BackendIE.BackendIE.Models.OllamaResponse;
import com.BackendIE.BackendIE.Repository.OllamaResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OllamaResponseService {

    @Autowired
    private OllamaResponseRepository ollamaResponseRepository;

    public List<OllamaResponse> getAll() {
        return ollamaResponseRepository.findAll();
    }

    public Optional<OllamaResponse> getById(String id) {
        return ollamaResponseRepository.findById(id);
    }

    public OllamaResponse save(OllamaResponse response) {
        return ollamaResponseRepository.save(response);
    }

    public OllamaResponse update(String id, OllamaResponse updatedResponse) {
        return ollamaResponseRepository.findById(id)
                .map(existing -> {
                    existing.setPregunta(updatedResponse.getPregunta());
                    existing.setRespuesta(updatedResponse.getRespuesta());
                    existing.setUsuarioId(updatedResponse.getUsuarioId());
                    existing.setEmpresaId(updatedResponse.getEmpresaId());
                    return ollamaResponseRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("No se encontró la respuesta con id " + id));
    }

    public void delete(String id) {
        ollamaResponseRepository.deleteById(id);
    }
}
