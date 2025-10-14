package com.BackendIE.BackendIE.Models;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "protocolos")
@Getter
@Setter
@NoArgsConstructor
public class Protocolo {

    @Id
    private String id;

    private String nombre;
    private String descripcion;
    private Long empresaId;
    private String objetivo;
    private List<String> reglas;

    public Protocolo(String nombre, String descripcion, Long empresaId, String objetivo, List<String> reglas) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.empresaId = empresaId;
        this.objetivo = objetivo;
        this.reglas = reglas;
    }
}
