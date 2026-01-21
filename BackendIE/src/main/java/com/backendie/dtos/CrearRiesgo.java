package com.backendie.dtos;

import lombok.Data;
import java.util.List;

/**
 * DTO usado para generar riesgos automáticamente con Ollama.
 * Incluye los datos de contexto necesarios (empresa, usuario, políticas, etc.).
 */
@Data
public class CrearRiesgo {

    private Long empresaId;               // ID de la empresa
    private Long usuarioId;               // ID del usuario solicitante
    private String objetivo;              // Objetivo general del análisis de riesgo
    private List<Long> idsDePoliticas;    // Políticas o documentos que servirán de contexto

}
