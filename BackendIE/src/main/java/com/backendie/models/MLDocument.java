package com.backendie.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "ml_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MLDocument {
    @Id
    private String id;
    private String filename;
    private Long empresaId;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
    private String contentType;
    private Long size;
    private String notes;
}

