package com.backendie.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "regulaciones")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Regulacion {

    @Id
    private String id; // En MongoDB se usa String (ObjectId por defecto)
    private String nombre;
    private String contenido;
    private String urlDocumento;
    private String entidadEmisora;
    private Integer anioEmision;

    public Regulacion(String nombre, String contenido, String urlDocumento, String entidadEmisora, Integer anioEmision) {
        this.nombre = nombre;
        this.contenido = contenido;
        this.urlDocumento = urlDocumento;
        this.entidadEmisora = entidadEmisora;
        this.anioEmision = anioEmision;
    }
}
