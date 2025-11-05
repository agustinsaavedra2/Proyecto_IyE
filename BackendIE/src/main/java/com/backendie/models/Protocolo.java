package com.backendie.models;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "protocolos")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Protocolo {

    @Id
    private String idProtocolo;

    private String nombre;
    private String descripcion;
    private Long empresaId;
    private String objetivo;
    private List<String> reglas;
    private String politicaId;
}
