package com.backendie.models;

import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "procedimientos")
@Data
@NoArgsConstructor
public class Procedimiento {

    @Id
    private String id;

    private String nombre;
    private String descripcion;
    private Long empresaId;
    private String objetivo;
    private List<String> pasos;
    private String protocoloId;

    public Procedimiento(String protocoloId, List<String> pasos, String objetivo, Long empresaId, String descripcion, String nombre) {
        this.protocoloId = protocoloId;
        this.pasos = pasos;
        this.objetivo = objetivo;
        this.empresaId = empresaId;
        this.descripcion = descripcion;
        this.nombre = nombre;
    }
}

