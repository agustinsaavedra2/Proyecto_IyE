package com.backendie.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Document(collection = "politicasEmpresas")
public class PoliticaEmpresa {

    @Id
    private String id; // Mongo usa String para _id

    private Long empresaId;

    private String titulo;

    private String contenido;

    private Boolean aiGenerada = false;

    private String aiModeloVersion;

    private Double complianceScore;

    private String estado = "draft";

    private String version = "1.0";

    private Long aprobadoPor;

    private LocalDateTime fechaAprobacion;

    private LocalDateTime createDat = LocalDateTime.now();

    private LocalDateTime updateDat = LocalDateTime.now();

    private LocalDateTime deleteDat; // Soft delete
}
