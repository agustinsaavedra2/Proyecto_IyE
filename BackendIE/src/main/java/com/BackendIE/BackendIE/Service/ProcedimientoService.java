package com.BackendIE.BackendIE.Service;

import com.BackendIE.BackendIE.Models.Empresa;
import com.BackendIE.BackendIE.Models.Procedimiento;
import com.BackendIE.BackendIE.Models.Protocolo;
import com.BackendIE.BackendIE.Repository.*;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ProcedimientoService {

    @Autowired
    private OllamaResponseRepository ollamaResponseRepository;


    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private CategoriaIndustriaRepository categoriaIndustriaRepository;

    @Autowired
    private RegulacionRepository regulacionRepository;

    @Autowired
    private OllamaChatModel ollamaChatModel;

    @Autowired
    private PoliticaEmpresaRepository politicaEmpresaRepository;

    @Autowired
    private ProtocoloRepository protocoloRepository;

    @Autowired
    private ProcedimientoRepository procedimientoRepository;

    @Autowired
    private ConfigurableEnvironment env;

    @Autowired
    private EmpresaService empresaService;


    public Procedimiento crearProcedimiento(Long empresaId, Long usuarioId, String protocoloId, String tema) {
        // 1️⃣ Validar empresa y usuario
        Empresa empresa = empresaService.validarEmpresaYUsuario(empresaId, usuarioId);

        // 2️⃣ Buscar el protocolo asociado
        Protocolo protocolo = protocoloRepository.findById(protocoloId)
                .orElseThrow(() -> new IllegalArgumentException("Protocolo no encontrado con ID: " + protocoloId));

        // 3️⃣ Construir el prompt para la IA
        String prompt = """
        Actúa como un Especialista en Normalización de Procesos Empresariales.
        Basado en el siguiente protocolo, redacta un **procedimiento operativo detallado y formal** sobre el tema '%s',
        aplicable a la empresa '%s'.

        **Protocolo base:**
        %s

        **Instrucciones de Generación:**
        - El procedimiento debe describir cómo ejecutar en la práctica las reglas o pasos del protocolo.
        - Debe incluir, como mínimo:
          - Nombre del procedimiento
          - Descripción breve
          - Objetivo específico
          - Lista de pasos o acciones concretas
        - Usa tono técnico, impersonal y formal.
        - No incluyas texto fuera del formato (sin comentarios, sin markdown, sin negritas ni JSON).

        **Instrucciones de Formato de Salida (usa exactamente estos separadores):**

        ::PROCEDIMIENTO::
        ::nombre:: [Nombre del procedimiento]
        ::descripcion:: [Descripción breve y clara del propósito del procedimiento]
        ::objetivo:: [Objetivo específico del procedimiento]
        ::pasos:: [Lista de pasos concretos, separados por ;;]
        ::END_PROCEDIMIENTO::

        Devuelve únicamente el texto anterior, sin texto adicional antes ni después.
        """.formatted(
                tema,
                empresa.getNombre(),
                protocolo.getDescripcion()
        );

        // 4️⃣ Llamada al modelo IA
        String respuesta;
        try {
            respuesta = ollamaChatModel.call(prompt);
        } catch (Exception e) {
            throw new IllegalStateException("Error al invocar el modelo IA: " + e.getMessage());
        }

        // 5️⃣ Limpieza mínima por seguridad (por si hay markdown o negritas)
        respuesta = respuesta.replaceAll("\\*\\*", "").trim();

        // 6️⃣ Extracción con delimitadores
        String nombre = extractValue(respuesta, "::nombre::", "::descripcion::");
        String descripcion = extractValue(respuesta, "::descripcion::", "::objetivo::");
        String objetivo = extractValue(respuesta, "::objetivo::", "::pasos::");
        String pasosRaw = extractValue(respuesta, "::pasos::", "::END_PROCEDIMIENTO::");

        List<String> pasos = Arrays.stream(pasosRaw.split(";;"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        // 7️⃣ Validación
        if (nombre.isBlank() || descripcion.isBlank() || objetivo.isBlank() || pasos.isEmpty()) {
            System.err.println("⚠️ Respuesta IA no válida o incompleta:\n" + respuesta);
            throw new IllegalStateException("La IA no devolvió un procedimiento válido. Revisa el formato o la respuesta cruda.");
        }

        // 8️⃣ Crear y guardar el procedimiento
        Procedimiento procedimiento = new Procedimiento(protocoloId, pasos, objetivo, empresaId, descripcion, nombre);
        return procedimientoRepository.save(procedimiento);
    }

    /**
     * Extrae texto entre delimitadores ::inicio:: y ::fin:: de forma segura.
     */
    private String extractValue(String text, String start, String endDelimiter) {
        if (text == null || start == null) return "";
        int s = text.indexOf(start);
        if (s == -1) return "";
        s += start.length();

        int e = text.length(); // por defecto hasta el final
        if (endDelimiter != null) {
            int next = text.indexOf(endDelimiter, s);
            if (next != -1) e = next;
        } else {
            // Buscar el siguiente campo (::campo::) o fin
            int nextCampo = text.indexOf("::", s);
            if (nextCampo != -1) e = nextCampo;
        }

        return text.substring(s, e).trim();
    }



}
