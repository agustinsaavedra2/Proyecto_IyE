package com.BackendIE.BackendIE.Service;

import com.BackendIE.BackendIE.Models.Empresa;
import com.BackendIE.BackendIE.Models.PoliticaEmpresa;
import com.BackendIE.BackendIE.Models.Protocolo;
import com.BackendIE.BackendIE.Repository.*;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ProtocoloService {

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

    public Protocolo crearProtocolo(Long empresaId, Long usuarioId, String politicaId, String tema) {
        // 1️⃣ Validar empresa y usuario
        Empresa empresa = empresaService.validarEmpresaYUsuario(empresaId, usuarioId);

        // 2️⃣ Buscar la política en Mongo por su ID (tipo String)
        PoliticaEmpresa politica = politicaEmpresaRepository.findById(politicaId)
                .orElseThrow(() -> new IllegalArgumentException("Política no encontrada con ID: " + politicaId));

        // 3️⃣ Construir el prompt para la IA
        String prompt = """
        Actúa como un Especialista en Implementación de Políticas Operativas.
        Tu tarea es redactar un **protocolo práctico y formal** para aplicar la política empresarial asociada, sobre el tema '%s', para la empresa '%s'.

        **Política Base:**
        %s
        
        **Instrucciones de Generación:**
        - El protocolo debe describir cómo aplicar la política en la práctica.
        - Debe incluir, como mínimo:
          - Nombre del protocolo
          - Descripción breve
          - Objetivo principal
          - Lista de reglas o pasos de aplicación
        - Redacta en tono formal, impersonal y técnico.
        - No incluyas explicaciones, ejemplos ni comentarios fuera del formato.
        - No uses markdown, negritas, encabezados, JSON ni texto decorativo.
        
        **Instrucciones de Formato de Salida (usa exactamente estos separadores):**
        
        ::PROTOCOLO::
        ::nombre:: [Nombre del protocolo]
        ::descripcion:: [Descripción breve y clara del propósito del protocolo]
        ::objetivo:: [Objetivo principal del protocolo]
        ::reglas:: [Lista de reglas o pasos, separados por ;;]
        ::END_PROTOCOLO::
        
        Devuelve únicamente el texto anterior, sin texto adicional antes ni después.
        """.formatted(
                tema,
                empresa.getNombre(),
                politica.getContenido()
        );

        // 4️⃣ Llamar al modelo IA (Ollama o similar)
        String respuesta = ollamaChatModel.call(prompt);

        // 5️⃣ Extraer valores delimitados
        String nombre = extractValue(respuesta, "::nombre::", "::descripcion::");
        String descripcion = extractValue(respuesta, "::descripcion::", "::objetivo::");
        String objetivo = extractValue(respuesta, "::objetivo::", "::reglas::");
        String reglasRaw = extractValue(respuesta, "::reglas::", "::END_PROTOCOLO::");

        List<String> reglas = Arrays.stream(reglasRaw.split(";;"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        // 6️⃣ Validar que la IA devolvió datos válidos
        if (nombre.isBlank() || descripcion.isBlank() || objetivo.isBlank() || reglas.isEmpty()) {
            System.err.println("⚠️ Respuesta IA no válida o incompleta:\n" + respuesta);
            throw new IllegalStateException("La IA no devolvió un protocolo válido. Revisa el formato o la respuesta cruda.");
        }

        // 7️⃣ Crear y guardar el protocolo
        Protocolo protocolo = new Protocolo(nombre, descripcion, empresaId, objetivo, reglas, politicaId);
        return protocoloRepository.save(protocolo);
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
            // Buscar el siguiente campo (::campo::) o el final del bloque
            int nextCampo = text.indexOf("::", s);
            if (nextCampo != -1) e = nextCampo;
        }

        return text.substring(s, e).trim();
    }


}
