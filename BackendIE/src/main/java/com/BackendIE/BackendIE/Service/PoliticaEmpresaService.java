package com.BackendIE.BackendIE.Service;

import com.BackendIE.BackendIE.Models.CategoriaIndustria;
import com.BackendIE.BackendIE.Models.Empresa;
import com.BackendIE.BackendIE.Models.PoliticaEmpresa;
import com.BackendIE.BackendIE.Models.Regulacion;
import com.BackendIE.BackendIE.Repository.*;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;

import java.util.List;

import java.util.stream.Collectors;
import java.util.Objects;


@Service
public class PoliticaEmpresaService {

    @Autowired
    private PoliticaEmpresaRepository politicaEmpresaRepository;


    @Autowired
    private CategoriaIndustriaRepository categoriaIndustriaRepository;

    @Autowired
    private RegulacionRepository regulacionRepository;

    @Autowired
    private OllamaChatModel ollamaChatModel;

    @Autowired
    private ConfigurableEnvironment env;

    @Autowired
    private EmpresaService empresaService;

    public PoliticaEmpresa crearPolitica(Long empresaId, Long usuarioId, String tema) {
        // 1️⃣ Validar empresa y usuario
        Empresa empresa = empresaService.validarEmpresaYUsuario(empresaId, usuarioId);
        CategoriaIndustria categoria = categoriaIndustriaRepository.findById(empresa.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada."));

        List<Regulacion> regulaciones = categoria.getRegulaciones().stream()
                .map(id -> regulacionRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        String regulacionesStr = regulaciones.stream()
                .map(Regulacion::getContenido)
                .collect(Collectors.joining("\n\n"));

        // 2️⃣ Construcción del prompt con formato de salida estructurado por delimitadores
        String prompt = """
            Actúa como un Asesor en Cumplimiento Normativo experto en el rubro: %s.
            Tu tarea es redactar una **política empresarial oficial** sobre el tema '%s' para la empresa '%s'.

            **Contexto de Regulaciones Relevantes:**
            %s

            **Instrucciones de Generación:**
            - Analiza las regulaciones y el contexto del rubro.
            - Redacta una política realista y completa que contenga como mínimo:
              - Objetivo
              - Alcance
              - Declaraciones o Principios
              - Responsabilidades
            - Evita explicaciones, comentarios o texto adicional fuera del formato solicitado.
            - No incluyas encabezados como "respuesta", "JSON" o "código".

            **Instrucciones de Formato de Salida (usa exactamente estos separadores):**

            ::POLITICA::
            ::titulo:: [Título conciso y claro de la política]
            ::contenido:: [Texto completo de la política: Objetivo, Alcance, Declaraciones, Responsabilidades]
            ::END_POLITICA::

            Devuelve únicamente el texto anterior, sin texto adicional antes ni después.
            """.formatted(
                categoria.getNombre(),
                tema,
                empresa.getNombre(),
                regulacionesStr
        );

        // 3️⃣ Llamada a la IA
        String respuesta = ollamaChatModel.call(prompt);

        // 4️⃣ Extracción de valores usando delimitadores
        String titulo = extractValue(respuesta, "::titulo::", "::contenido::");
        String contenido = extractValue(respuesta, "::contenido::", "::END_POLITICA::");

        // 5️⃣ Limpieza y validación
        if (titulo == null) titulo = "";
        if (contenido == null) contenido = "";

        titulo = titulo.replaceAll("^\\[(.*)]$", "$1").trim();
        contenido = contenido.replaceAll("^\\[(.*)]$", "$1").trim();

        if (titulo.isBlank() || contenido.isBlank()) {
            System.err.println("⚠️ Respuesta IA no válida:\n" + respuesta);
            throw new IllegalStateException(
                    "Error al crear la política: la IA no devolvió correctamente los campos 'titulo' y/o 'contenido'."
            );
        }

        // 6️⃣ Creación del objeto y persistencia
        PoliticaEmpresa politica = new PoliticaEmpresa();
        politica.setEmpresaId(empresaId);
        politica.setTitulo(titulo);
        politica.setContenido(contenido);
        politica.setAiGenerada(true);
        politica.setAiModeloVersion(env.getProperty("spring.ai.ollama.chat.model", "desconocido"));
        politica.setEstado("draft");

        return politicaEmpresaRepository.save(politica);
    }

    /**
     * Extrae texto entre delimitadores ::startTag:: y ::endTag::.
     */
    private String extractValue(String text, String startTag, String endTag) {
        try {
            if (text == null || startTag == null) return "";
            int startIndex = text.indexOf(startTag);
            if (startIndex == -1) return "";
            startIndex += startTag.length();

            int endIndex = (endTag != null) ? text.indexOf(endTag, startIndex) : -1;
            if (endIndex == -1) endIndex = text.length();

            return text.substring(startIndex, endIndex).trim();
        } catch (Exception e) {
            System.err.println("Error extrayendo valor entre " + startTag + " y " + endTag + ": " + e.getMessage());
            return "";
        }
    }



}
