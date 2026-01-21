package com.backendie.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

@RestController
@RequestMapping("/api/training")
@RequiredArgsConstructor
public class TrainingController {

    @PostMapping("/start")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> startTraining(@RequestParam(value="labelMethod", defaultValue="none") String labelMethod,
                                           @RequestParam(value="model", defaultValue="logreg") String model) {
        try {
            // Run trainer in ml/trainer.py (assumes Python environment in container or host)
            ProcessBuilder pb = new ProcessBuilder("python", "ml/trainer.py", "--index", "ml/index.jsonl", "--out-dir", "ml/models", "--label-method", labelMethod, "--model", model);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = r.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }
            int exit = p.waitFor();
            if (exit != 0) {
                return ResponseEntity.status(500).body(Map.of("error", "trainer failed", "output", sb.toString()));
            }
            return ResponseEntity.ok(Map.of("status","ok","output", sb.toString()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}

