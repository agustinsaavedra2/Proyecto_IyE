package com.backendie.service;

import com.backendie.models.MLDocument;
import com.backendie.repository.MLDocumentRepository;
import com.backendie.service.OllamaResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MLService {

    private final MLDocumentRepository mlDocumentRepository;
    private final OllamaResponseService ollamaResponseService;
    private final MLTrainingService mlTrainingService;
    private final MLModelService mlModelService; // new service

    private final Path storage = Path.of("./ml_storage");

    public MLDocument storeDocument(MultipartFile file, Long empresaId, String uploadedBy) throws IOException {
        Files.createDirectories(storage);
        String filename = System.currentTimeMillis() + "-" + file.getOriginalFilename();
        Path dest = storage.resolve(filename);
        file.transferTo(dest);
        MLDocument doc = MLDocument.builder()
                .filename(filename)
                .empresaId(empresaId)
                .uploadedBy(uploadedBy)
                .uploadedAt(LocalDateTime.now())
                .contentType(file.getContentType())
                .size(file.getSize())
                .build();
        return mlDocumentRepository.save(doc);
    }

    public List<MLDocument> listByEmpresa(Long empresaId) {
        return mlDocumentRepository.findByEmpresaId(empresaId);
    }

    // Simple NER wrapper that asks Ollama to return JSON with entities
    public Map<String,Object> extractEntitiesFromDocument(String text) {
        return ollamaResponseService.extractEntities(text);
    }

    // Placeholder for training: in this MVP we only register the dataset and call an external trainer (not implemented)
    public String startTrainingJob(Long empresaId) {
        try {
            return mlTrainingService.train(empresaId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String,Object> predictRisk(String text, Long empresaId, Long usuarioId) {
        // Prefer the Python ML model if artifacts exist
        try {
            if (mlModelService.modelArtifactsExist()) {
                return mlModelService.predict(text);
            }
        } catch (Exception ignored) {}
        // fallback to simple lexicon model
        return mlTrainingService.predict(text, empresaId, usuarioId);
    }
}