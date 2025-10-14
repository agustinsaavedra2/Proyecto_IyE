package com.BackendIE.BackendIE.Models;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "regulaciones")
@Data
@Getter
@Setter
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
