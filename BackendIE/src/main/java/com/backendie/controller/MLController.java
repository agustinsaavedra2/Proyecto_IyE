package com.backendie.controller;

import com.backendie.models.MLDocument;
import com.backendie.service.MLService;
import com.backendie.service.OllamaResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ml")
@RequiredArgsConstructor
public class MLController {

    private final MLService mlService;
    private final OllamaResponseService ollamaResponseService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
                                    @RequestParam("empresaId") Long empresaId,
                                    Authentication authentication) {
        try {
            String user = authentication != null ? authentication.getName() : "anonymous";
            MLDocument doc = mlService.storeDocument(file, empresaId, user);
            return ResponseEntity.ok(doc);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to store document: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<MLDocument>> list(@RequestParam("empresaId") Long empresaId) {
        return ResponseEntity.ok(mlService.listByEmpresa(empresaId));
    }

    @PostMapping("/train")
    public ResponseEntity<?> train(@RequestParam("empresaId") Long empresaId) {
        String jobId = mlService.startTrainingJob(empresaId);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

    @PostMapping("/ner")
    public ResponseEntity<?> ner(@RequestBody Map<String,String> body) {
        String text = body.getOrDefault("text",""
        );
        Map<String,Object> out = mlService.extractEntitiesFromDocument(text);
        return ResponseEntity.ok(out);
    }

    @PostMapping("/classify")
    public ResponseEntity<?> classify(@RequestBody Map<String,Object> body) {
        try {
            Long empresaId = Long.valueOf(String.valueOf(body.get("empresaId")));
            Long usuarioId = Long.valueOf(String.valueOf(body.get("usuarioId")));
            List<String> ids = (List<String>) body.getOrDefault("idsDePoliticas", List.of());
            Map<String,Object> out = ollamaResponseService.classifyRisk(empresaId, usuarioId, ids);
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/predict")
    public ResponseEntity<?> predict(@RequestBody Map<String,Object> body) {
        try {
            String text = String.valueOf(body.getOrDefault("text",""));
            Long empresaId = body.get("empresaId") == null ? null : Long.valueOf(String.valueOf(body.get("empresaId")));
            Long usuarioId = body.get("usuarioId") == null ? null : Long.valueOf(String.valueOf(body.get("usuarioId")));
            Map<String,Object> out = mlService.predictRisk(text, empresaId, usuarioId);
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}