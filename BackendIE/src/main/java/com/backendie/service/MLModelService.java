package com.backendie.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MLModelService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Path modelDir = Path.of("./ml/models");
    private final Path pythonScript = Path.of("./ml/predict.py");

    public boolean modelArtifactsExist() {
        return Files.exists(modelDir.resolve("model.joblib")) && Files.exists(modelDir.resolve("vectorizer.joblib"));
    }

    public Map<String,Object> predict(String text) throws Exception {
        if (!modelArtifactsExist()) throw new IllegalStateException("ML artifacts not found under ml/models");
        // Build command: python ml/predict.py "text"
        ProcessBuilder pb = new ProcessBuilder();
        pb.command("python", pythonScript.toString(), text);
        pb.directory(new File("."));
        pb.redirectErrorStream(true);
        Process p = pb.start();
        StringBuilder out = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                out.append(line).append('\n');
            }
        }
        int exit = p.waitFor();
        if (exit != 0) {
            throw new RuntimeException("Prediction script failed: " + out.toString());
        }
        String json = out.toString().trim();
        Map<String,Object> map = mapper.readValue(json, Map.class);
        return map;
    }
}

