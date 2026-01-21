package com.backendie.controller;

import com.backendie.service.AdminBackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminBackupService adminBackupService;

    @GetMapping("/backups")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> listBackups() {
        return ResponseEntity.ok(adminBackupService.listBackups());
    }

    @PostMapping("/backups/trigger")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> triggerBackup() {
        try {
            adminBackupService.triggerBackup();
            return ResponseEntity.ok().body("Backup triggered");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to trigger backup: " + e.getMessage());
        }
    }

    @DeleteMapping("/backups/{fileName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteBackup(@PathVariable String fileName) {
        try {
            boolean deleted = adminBackupService.deleteBackup(fileName);
            if (deleted) return ResponseEntity.ok().body("Deleted");
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to delete backup: " + e.getMessage());
        }
    }
}

