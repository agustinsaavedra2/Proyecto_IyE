package com.backendie.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "respuestaOllama")
@Data
@NoArgsConstructor
public class OllamaResponse {

    @Id
    private String id; // Mongo genera un ObjectId automáticamente

    private Long empresaId;
    private Long usuarioId;
    private String pregunta;
    private String respuesta;

    public OllamaResponse (Long empresaId, Long usuarioId, String pregunta, String respuesta) {
        this.empresaId = empresaId;
        this.usuarioId = usuarioId;
        this.pregunta = pregunta;
        this.respuesta = respuesta;
    }

}
