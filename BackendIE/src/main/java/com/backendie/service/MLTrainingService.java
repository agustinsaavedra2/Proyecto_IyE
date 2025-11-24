package com.backendie.service;

import com.backendie.models.MLDocument;
import com.backendie.models.Riesgo;
import com.backendie.repository.MLDocumentRepository;
import com.backendie.repository.RiesgoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MLTrainingService {

    private final MLDocumentRepository mlDocumentRepository;
    private final RiesgoRepository riesgoRepository;

    private final Path modelPath = Path.of("./ml_model.json");

    // Very simple keyword-based model that maps keywords to risk weights
    public String train(Long empresaId) throws IOException {
        // collect all documents for empresaId (or global if empresaId==null)
        List<MLDocument> docs = empresaId == null ? mlDocumentRepository.findAll() : mlDocumentRepository.findByEmpresaId(empresaId);

        Map<String, Integer> keywordCounts = new HashMap<>();
        // fallback: build keywords from documents contents in ml_storage if available
        Path storage = Path.of("./ml_storage");
        if (Files.exists(storage)) {
            Files.list(storage).forEach(p -> {
                try {
                    String content = Files.readString(p).toLowerCase();
                    // naive tokenization
                    Arrays.stream(content.split("\\W+"))
                            .filter(s -> s.length()>3)
                            .forEach(t -> keywordCounts.merge(t, 1, Integer::sum));
                } catch (Exception ignored) {}
            });
        }

        // Seed some risk keywords if keywordCounts empty
        if (keywordCounts.isEmpty()) {
            keywordCounts.put("incumplimiento", 10);
            keywordCounts.put("sancion", 8);
            keywordCounts.put("multa", 7);
            keywordCounts.put("riesgo", 6);
            keywordCounts.put("no cumple", 5);
            keywordCounts.put("higiene", 4);
            keywordCounts.put("inspeccion", 3);
        }

        // create a simple model object
        Map<String,Object> model = new HashMap<>();
        model.put("createdAt", LocalDateTime.now().toString());
        model.put("empresaId", empresaId);
        // keep top 200 keywords
        List<Map.Entry<String,Integer>> top = keywordCounts.entrySet().stream()
                .sorted((a,b)->b.getValue().compareTo(a.getValue()))
                .limit(200)
                .collect(Collectors.toList());
        Map<String,Integer> lex = new HashMap<>();
        for (Map.Entry<String,Integer> e : top) lex.put(e.getKey(), e.getValue());
        model.put("lexicon", lex);

        // persist model
        String json = new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(model);
        Files.createDirectories(modelPath.getParent()==null?Path.of("."):modelPath.getParent());
        Files.writeString(modelPath, json);
        return "trained:" + System.currentTimeMillis();
    }

    public Map<String,Object> predict(String text, Long empresaId, Long usuarioId) {
        Map<String,Object> out = new HashMap<>();
        Map<String,Integer> lexicon = loadLexicon();
        String lower = text == null ? "" : text.toLowerCase();
        int score = 0;
        Map<String,Integer> hits = new HashMap<>();
        for (Map.Entry<String,Integer> e : lexicon.entrySet()) {
            String kw = e.getKey().toLowerCase();
            if (lower.contains(kw)) {
                int w = e.getValue();
                score += w;
                hits.put(kw, w);
            }
        }
        // normalize score to 0-100
        double norm = Math.min(100.0, score);
        String risk = norm > 40 ? "Alto" : norm > 15 ? "Medio" : "Bajo";
        double confidence = Math.min(99.0, Math.max(30.0, norm));

        out.put("risk", risk);
        out.put("confidence", confidence);
        out.put("explanation", "keywords matched: " + hits.keySet());
        out.put("hits", hits);
        out.put("raw_score", score);

        // Persist result as Riesgo for auditability
        Riesgo r = new Riesgo();
        r.setEmpresaId(empresaId);
        r.setTitulo("Evaluación ML - regla");
        r.setDescripcion("Clasificación por modelo de palabras clave");
        r.setCategoria("compliance");
        r.setProbabilidad(confidence>66?"alta":confidence>33?"media":"baja");
        r.setImpacto("medio");
        r.setNivelRiesgo(risk);
        r.setMedidasMitigacion(out.get("explanation").toString());
        r.setResponsable(usuarioId);
        r.setEstado("evaluado");
        riesgoRepository.save(r);
        out.put("riesgoId", r.getId());
        return out;
    }

    private Map<String,Integer> loadLexicon() {
        try {
            if (!Files.exists(modelPath)) return Map.of();
            String json = Files.readString(modelPath);
            Map map = new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, Map.class);
            Map<String,Integer> lex = (Map<String,Integer>) map.get("lexicon");
            return lex==null?Map.of():lex;
        } catch (Exception e) {
            return Map.of();
        }
    }
}
