package com.backendie.controller;

import com.backendie.service.DocumentService;
import com.backendie.service.EmailService;
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
    private final EmailService emailService;

    @GetMapping("/politica/{id}/pdf")
    public ResponseEntity<byte[]> getPoliticaPdf(@PathVariable String id) {
        byte[] pdf = documentService.renderPoliticaToPdf(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=politica_" + id + ".pdf")
                .body(pdf);
    }

    @PostMapping("/politica/{id}/send-email")
    public ResponseEntity<?> sendPoliticaByEmail(@PathVariable String id, @RequestParam("to") String to,
                                                 @RequestParam(value = "subject", required = false) String subject) {
        try {
            byte[] pdf = documentService.renderPoliticaToPdf(id);
            String subj = subject == null ? "Política generada" : subject;
            String body = "Adjunto se envía la política generada para su revisión.";
            emailService.sendEmailWithAttachment(to, subj, body, pdf, "politica_" + id + ".pdf");
            return ResponseEntity.accepted().body("Email queued to: " + to);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send PDF by email: " + e.getMessage());
        }
    }
}
