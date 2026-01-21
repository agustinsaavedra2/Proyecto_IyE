package com.backendie.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
        // Try HTTP service first (configurable via env ML_SERVICE_URL)
        String mlUrl = System.getenv("ML_SERVICE_URL");
        if (mlUrl != null && !mlUrl.isBlank()) {
            try {
                return predictViaHttp(mlUrl, text);
            } catch (Exception e) {
                // Log and fallback to local script
                System.err.println("Warning: calling ML service failed, falling back to local script: " + e.getMessage());
            }
        }

        // Fallback: local script execution
        if (!modelArtifactsExist()) throw new IllegalStateException("ML artifacts not found under ml/models");
        ProcessBuilder pb = new ProcessBuilder();
        pb.command("python", pythonScript.toString(), text);
        pb.directory(new File("."));
        pb.redirectErrorStream(true);
        Process p = pb.start();
        StringBuilder out = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
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

    private Map<String,Object> predictViaHttp(String baseUrl, String text) throws Exception {
        URL url = new URL(baseUrl + "/predict");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        con.setConnectTimeout(5000);
        con.setReadTimeout(10000);
        con.setDoOutput(true);
        String payload = mapper.writeValueAsString(Map.of("text", text));
        byte[] out = payload.getBytes(StandardCharsets.UTF_8);
        con.getOutputStream().write(out);
        int status = con.getResponseCode();
        BufferedReader in;
        if (status >= 200 && status < 300) {
            in = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
        } else {
            in = new BufferedReader(new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8));
        }
        StringBuilder resp = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            resp.append(line).append('\n');
        }
        in.close();
        if (status < 200 || status >= 300) {
            throw new RuntimeException("ML service returned status=" + status + " body=" + resp.toString());
        }
        Map<String,Object> map = mapper.readValue(resp.toString(), Map.class);
        return map;
    }
}
