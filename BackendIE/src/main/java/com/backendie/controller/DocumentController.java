package com.backendie.controller;

import com.backendie.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping("/politica/{id}/pdf")
    public ResponseEntity<byte[]> getPoliticaPdf(@PathVariable String id) {
        byte[] pdf = documentService.renderPoliticaToPdf(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=politica_" + id + ".pdf")
                .body(pdf);
    }
}

