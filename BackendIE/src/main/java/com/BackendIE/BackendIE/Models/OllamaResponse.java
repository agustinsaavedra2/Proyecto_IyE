package com.BackendIE.BackendIE.Models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name="ollamaResponses")
@Getter
@Setter
@NoArgsConstructor
public class OllamaResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="usuarioId")
    private Long usuarioId;

    @Column(name="pregunta")
    private String pregunta;

    @Column(name="respuesta")
    private String respuesta;

    @Column(name="modelo")
    private String modelo;

    @Column(name="createdAt")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="updatedAt")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name="deletedAt")
    private LocalDateTime deletedAt;

    public OllamaResponse(Long usuarioId, String pregunta, String respuesta, String modelo) {
        this.usuarioId = usuarioId;
        this.pregunta = pregunta;
        this.respuesta = respuesta;
        this.modelo = modelo;
    }
}
