package com.backendie.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminBackupService {

    @Value("${backup.dir:./backups}")
    private String backupsDir;

    public List<String> listBackups() {
        File dir = new File(backupsDir);
        if (!dir.exists() || !dir.isDirectory()) return List.of();
        return Arrays.stream(dir.listFiles())
                .filter(File::isFile)
                .sorted(Comparator.comparingLong(File::lastModified).reversed())
                .map(File::getName)
                .collect(Collectors.toList());
    }

    public boolean triggerBackup() throws IOException {
        Path trigger = Path.of(backupsDir, "trigger");
        Files.createDirectories(trigger.getParent());
        if (!Files.exists(trigger)) {
            Files.writeString(trigger, "triggered at " + Instant.now().toString());
        }
        return true;
    }

    public boolean deleteBackup(String fileName) throws IOException {
        Path p = Path.of(backupsDir, fileName);
        return Files.deleteIfExists(p);
    }
}

