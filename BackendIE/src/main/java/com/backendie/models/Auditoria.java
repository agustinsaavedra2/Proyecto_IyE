package com.backendie.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "auditorias")
@Data
@NoArgsConstructor
public class Auditoria {

    @Id
    private String id; // En Mongo el ID es tipo String por defecto (ObjectId)

    private Long empresaId;
    private String tipo; // interno, externo, regulatorio
    private String objetivo;
    private String alcance;
    private Long auditorLider;
    private LocalDateTime fecha = LocalDateTime.now();
    private Double score; // 0.0 - 100.0
    private List<String> hallazgosCriticosMsj;
    private List<String> hallazgosMayoresMsj;
    private List<String> hallazgosMenoresMsj;
    private String recomendaciones;

    public Auditoria(
            Long empresaId,
            String tipo,
            String objetivo,
            String alcance,
            Long auditorLider,
            LocalDateTime fecha,
            Double score,
            List<String> hallazgosCriticosMsj,
            List<String> hallazgosMayoresMsj,
            List<String> hallazgosMenoresMsj,
            String recomendaciones
    ) {
        this.empresaId = empresaId;
        this.tipo = tipo;
        this.objetivo = objetivo;
        this.alcance = alcance;
        this.auditorLider = auditorLider;
        this.fecha = fecha;
        this.score = score;
        this.hallazgosCriticosMsj = hallazgosCriticosMsj;
        this.hallazgosMayoresMsj = hallazgosMayoresMsj;
        this.hallazgosMenoresMsj = hallazgosMenoresMsj;
        this.recomendaciones = recomendaciones;
    }
}
