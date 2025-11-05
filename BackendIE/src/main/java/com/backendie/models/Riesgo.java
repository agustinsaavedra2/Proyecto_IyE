package com.backendie.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Document(collection = "riesgos")
@Data
@NoArgsConstructor
public class Riesgo {

    @Id
    private String id; // En MongoDB el id es String (ObjectId)

    private Long empresaId;
    private String titulo;
    private String descripcion;
    private String categoria; // operativo, financiero, estratégico, cumplimiento
    private String probabilidad; // baja, media, alta
    private String impacto; // bajo, medio, alto
    private String nivelRiesgo; // bajo, medio, alto
    private String medidasMitigacion;
    private Long responsable;
    private String estado; // abierto, en progreso, mitigado, cerrado

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private LocalDateTime deletedAt;

    public Riesgo(Long empresaId, String titulo, String descripcion, String categoria, String probabilidad,
                  String impacto, String nivelRiesgo, String medidasMitigacion, Long responsable, String estado) {
        this.empresaId = empresaId;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.probabilidad = probabilidad;
        this.impacto = impacto;
        this.nivelRiesgo = nivelRiesgo;
        this.medidasMitigacion = medidasMitigacion;
        this.responsable = responsable;
        this.estado = estado;
    }
}
