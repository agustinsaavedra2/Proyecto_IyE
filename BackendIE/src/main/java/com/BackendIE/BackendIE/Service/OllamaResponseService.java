package com.BackendIE.BackendIE.Service;

import com.BackendIE.BackendIE.Models.*;
import com.BackendIE.BackendIE.Repository.*;
import com.BackendIE.BackendIE.Models.PoliticaEmpresa;
import com.BackendIE.BackendIE.Models.Protocolo;
import com.BackendIE.BackendIE.Models.Procedimiento;
import com.BackendIE.BackendIE.Repository.ProcedimientoRepository;
import com.BackendIE.BackendIE.Repository.ProtocoloRepository;
import com.BackendIE.BackendIE.Repository.PoliticaEmpresaRepository;
import org.springframework.core.env.ConfigurableEnvironment; // Para leer properties
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OllamaResponseService {

    @Autowired
    private OllamaResponseRepository ollamaResponseRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

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
    private AuditoriaRepository auditoriaRepository;

    @Autowired
    private RiesgoRepository riesgoRepository;


    public List<OllamaResponse> getAll() {
        return ollamaResponseRepository.findAll();
    }

    public Optional<OllamaResponse> getById(String id) {
        return ollamaResponseRepository.findById(id);
    }

    public OllamaResponse save(OllamaResponse response) {
        return ollamaResponseRepository.save(response);
    }


    public OllamaResponse update(String id, OllamaResponse updatedResponse) {
        return ollamaResponseRepository.findById(id)
                .map(existing -> {
                    existing.setPregunta(updatedResponse.getPregunta());
                    existing.setRespuesta(updatedResponse.getRespuesta());
                    existing.setUsuarioId(updatedResponse.getUsuarioId());
                    existing.setEmpresaId(updatedResponse.getEmpresaId());
                    return ollamaResponseRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("No se encontró la respuesta con id " + id));
    }

    public void delete(String id) {
        ollamaResponseRepository.deleteById(id);
    }

/**

    public OllamaResponse crearPPP(Long empresaId, Long usuarioId, String pregunta) {
        if (empresaId == null || usuarioId == null ||  pregunta.isEmpty()) {
            throw new IllegalArgumentException("Los campos empresaId, usuarioId y pregunta son obligatorios.");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        if (!usuario.getEmpresaId().equals(empresaId)) {
            throw new IllegalArgumentException("Usuario no pertenece a la empresa.");
        }

        if (empresa.getStatus() == null || !empresa.getStatus().equals("active")) {
            throw new IllegalArgumentException("La empresa debe estar activa para generar documentación.");
        }

        CategoriaIndustria categoria = categoriaIndustriaRepository.findById(empresa.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría de industria no encontrada."));

        List<String> regulacionesIds = categoria.getRegulaciones();

        List<Regulacion> regulaciones = regulacionesIds.stream()
                .map(id -> regulacionRepository.findById(id).orElse(null))
                .filter(r -> r != null)
                .toList();

        String regulacionesStr = "";
        for (Regulacion regulacion : regulaciones) {
            regulacionesStr = regulacionesStr + "\n\n" + regulacion.getContenido();
        }

        String prompt = "Actúa como un experto en consultoría de cumplimiento normativo y excelencia operativa para la industria de: " + categoria.getNombre() +".\n" +
                "    Tu tarea es generar un paquete de documentación completo para la empresa "+ empresa.getNombre() + "sobre el tema: "+ pregunta + "'.\n" +
                "\n" +
                "    **Contexto Regulatorio Obligatorio (Leyes y Normas):**\n" +
                "    Debes basar tus respuestas en las siguientes regulaciones. Si una regulación no aplica, ignórala. Si aplica, incorpórala en el contenido:\n" +
                regulacionesStr + "\n" +
                "**Instrucciones de Formato de Salida:**\n" +
                "    Debes generar UNA política, UN protocolo principal y AL MENOS UN procedimiento detallado.\n" +
                "    Usa los siguientes separadores EXACTOS para cada sección y campo. No agregues texto fuera de esta estructura.\n" +
                "\n" +
                "    ::POLITICA::\n" +
                "    ::titulo:: [Genera un título claro y conciso para la política]\n" +
                "    ::contenido:: [Genera el contenido completo de la política. Debe incluir: 1. Objetivo. 2. Alcance (a quién aplica). 3. Declaraciones de la política (las reglas generales). 4. Responsabilidades (Gerencia, Empleados).]\n" +
                "    ::END_POLITICA::\n" +
                "\n" +
                "    ::PROTOCOLO::\n" +
                "    ::nombre:: [Genera un nombre para el protocolo, ej: \"Protocolo de Gestión de Alérgenos\"]\n" +
                "    ::descripcion:: [Genera una breve descripción del protocolo]\n" +
                "    ::objetivo:: [Genera el objetivo principal del protocolo]\n" +
                "    ::reglas:: [Genera una lista de reglas clave del protocolo. Separa cada regla con ;;]\n" +
                "    ::END_PROTOCOLO::\n" +
                "\n" +
                "    ::PROCEDIMIENTO::\n" +
                "    ::nombre:: [Genera un nombre para el primer procedimiento, ej: \"Procedimiento de Recepción y Almacenamiento de Alérgenos\"]\n" +
                "    ::descripcion:: [Genera una breve descripción del procedimiento]\n" +
                "    ::objetivo:: [Genera el objetivo específico de este procedimiento]\n" +
                "    ::pasos:: [Genera la lista de pasos detallados (el \"cómo se hace\"). Separa cada paso con ;;]\n" +
                "    ::END_PROCEDIMIENTO::\n" +
                "    \"\"\"";

        String respuesta = ollamaChatModel.call(prompt);

        // --- INICIO DE NUEVA LÓGICA ---
        // Parsear y guardar la documentación generada
        try {
            // El modelo de PoliticaEmpresa es JPA (SQL) y los otros son Mongo
            // pero la lógica de parseo funciona igual.
            parseAndSaveDocumentation(respuesta, empresaId);
        } catch (Exception e) {
            // Es buena idea registrar el error, pero aun así guardar la respuesta cruda
            // para depuración.
            System.err.println("Error crítico al parsear y guardar la documentación de Ollama: " + e.getMessage());
            e.printStackTrace();
        }
        // --- FIN DE NUEVA LÓGICA ---

        OllamaResponse ollamaResponse = new OllamaResponse(empresaId, usuarioId, pregunta, respuesta);
        return ollamaResponseRepository.save(ollamaResponse);
    }


    private void parseAndSaveDocumentation(String respuesta, Long empresaId) {

        // Obtener el nombre del modelo de AI desde application.properties
        String aiModel = env.getProperty("spring.ai.ollama.chat.model", "desconocido");

        // --- 1. Parsear y Guardar Política (JPA Entity) ---
        String politicaBlock = extractValue(respuesta, "::POLITICA::", "::END_POLITICA::");
        String politicaTitulo = extractValue(politicaBlock, "::titulo::", null);
        String politicaContenido = extractValue(politicaBlock, "::contenido::", null);

        PoliticaEmpresa politica = new PoliticaEmpresa();
        politica.setEmpresaId(empresaId);
        politica.setTitulo(politicaTitulo);
        politica.setContenido(politicaContenido);
        politica.setAiGenerada(true);
        politica.setAiModeloVersion(aiModel);
        politica.setEstado("draft"); // Estado inicial por defecto

        // Guardamos la política y obtenemos su ID para enlazarla al protocolo
        PoliticaEmpresa politicaGuardada = politicaEmpresaRepository.save(politica);
        String politicaId = politicaGuardada.getId();

        // --- 2. Parsear y Guardar Protocolo (Mongo Document) ---
        String protocoloBlock = extractValue(respuesta, "::PROTOCOLO::", "::END_PROTOCOLO::");
        String protoNombre = extractValue(protocoloBlock, "::nombre::", null);
        String protoDesc = extractValue(protocoloBlock, "::descripcion::", null);
        String protoObjetivo = extractValue(protocoloBlock, "::objetivo::", null);
        String protoReglasRaw = extractValue(protocoloBlock, "::reglas::", null);

        List<String> protoReglas = protoReglasRaw.isEmpty()
                ? Collections.emptyList()
                : Arrays.stream(protoReglasRaw.split(";;")).map(String::trim).toList(); // Separa y limpia

        Protocolo protocolo = new Protocolo(
                protoNombre,
                protoDesc,
                empresaId,
                protoObjetivo,
                protoReglas,
                politicaId // Enlazamos la política recién creada
        );

        // Guardamos el protocolo y obtenemos su ID para enlazarlo al procedimiento
        Protocolo protocoloGuardado = protocoloRepository.save(protocolo);
        String protocoloId = protocoloGuardado.getId();

        // --- 3. Parsear y Guardar Procedimiento(s) (Mongo Document) ---
        // Usamos Regex para encontrar TODOS los bloques de procedimiento
        Pattern procPattern = Pattern.compile("::PROCEDIMIENTO::(.*?)::END_PROCEDIMIENTO::", Pattern.DOTALL);
        Matcher procMatcher = procPattern.matcher(respuesta);

        while (procMatcher.find()) {
            String procBlock = procMatcher.group(1);
            String procNombre = extractValue(procBlock, "::nombre::", null);
            String procDesc = extractValue(procBlock, "::descripcion::", null);
            String procObjetivo = extractValue(procBlock, "::objetivo::", null);
            String procPasosRaw = extractValue(procBlock, "::pasos::", null);

            List<String> procPasos = procPasosRaw.isEmpty()
                    ? Collections.emptyList()
                    : Arrays.stream(procPasosRaw.split(";;")).map(String::trim).toList(); // Separa y limpia

            Procedimiento procedimiento = new Procedimiento(
                    protocoloId, // Enlazamos el protocolo recién creado
                    procPasos,
                    procObjetivo,
                    empresaId,
                    procDesc,
                    procNombre
            );

            procedimientoRepository.save(procedimiento);
        }
    }
     */

public OllamaResponse crearAuditoria(Long empresaId, String tipo, String objetivo,
                                     Long auditorLiderId, List<String> idsDePoliticasAEvaluar) {

    // 1. Validación
    if (empresaId == null || tipo == null || tipo.isBlank() ||
            objetivo == null || objetivo.isBlank() ||
            auditorLiderId == null ||
            idsDePoliticasAEvaluar == null || idsDePoliticasAEvaluar.isEmpty()) {
        throw new IllegalArgumentException("Todos los campos son obligatorios, incluyendo al menos una política.");
    }

    Empresa empresa = empresaRepository.findById(empresaId)
            .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));

    if (empresa.getStatus() == null || !empresa.getStatus().equalsIgnoreCase("active")) {
        throw new IllegalArgumentException("La empresa debe estar activa para generar documentación.");
    }

    Usuario auditorLider = usuarioRepository.findById(auditorLiderId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario (auditor líder) no encontrado."));
    if (!auditorLider.getEmpresaId().equals(empresaId)) {
        throw new IllegalArgumentException("El auditor líder no pertenece a la empresa especificada.");
    }

    // 2. Contexto
    StringBuilder documentacionCompleta = new StringBuilder();

    // 🔹 Ahora ya son String, no hay que convertir
    List<PoliticaEmpresa> politicas = politicaEmpresaRepository.findAllById(idsDePoliticasAEvaluar);

    for (PoliticaEmpresa politica : politicas) {
        documentacionCompleta.append("\n\n--- INICIO POLITICA: ").append(politica.getTitulo())
                .append(" (ID: ").append(politica.getId()).append(") ---\n")
                .append(politica.getContenido())
                .append("\n--- FIN POLITICA: ").append(politica.getTitulo()).append(" ---\n");

        List<Protocolo> protocolos = protocoloRepository.findByIdPolitica(politica.getId()); // <- String
        for (Protocolo proto : protocolos) {
            documentacionCompleta.append("\n  -- INICIO PROTOCOLO: ").append(proto.getNombre())
                    .append(" (ID: ").append(proto.getIdProtocolo()).append(") --\n")
                    .append("  Descripcion: ").append(proto.getDescripcion()).append("\n")
                    .append("  Objetivo: ").append(proto.getObjetivo()).append("\n")
                    .append("  Reglas: ").append(String.join(";; ", proto.getReglas())).append("\n");

            List<Procedimiento> procedimientos =
                    procedimientoRepository.findByProtocoloId(proto.getIdProtocolo()); // <- String
            for (Procedimiento proc : procedimientos) {
                documentacionCompleta.append("\n    - INICIO PROCEDIMIENTO: ").append(proc.getNombre())
                        .append(" (ID: ").append(proc.getId()).append(") -\n")
                        .append("    Descripcion: ").append(proc.getDescripcion()).append("\n")
                        .append("    Objetivo: ").append(proc.getObjetivo()).append("\n")
                        .append("    Pasos: ").append(String.join(";; ", proc.getPasos())).append("\n")
                        .append("    - FIN PROCEDIMIENTO -\n");
            }
            documentacionCompleta.append("  -- FIN PROTOCOLO --\n");
        }
    }

    if (documentacionCompleta.isEmpty()) {
        throw new IllegalArgumentException("No se encontró documentación (políticas, protocolos o procedimientos) para los IDs proporcionados.");
    }

    // 3. Prompt
    String prompt = "Actúa como un Auditor de Cumplimiento experto y meticuloso.\n" +
            "Tu tarea es realizar una auditoría de tipo '" + tipo + "' para la empresa '" + empresa.getNombre() +
            "' con el siguiente objetivo: '" + objetivo + "'.\n\n" +
            "**Documentación a Auditar (Contexto):**\n" + documentacionCompleta + "\n\n" +
            "**Instrucciones de Formato de Salida:**\n" +
            "Analiza la documentación y genera un informe estructurado usando los siguientes separadores EXACTOS.\n" +
            "\n" +
            "::SCORE:: [Genera un puntaje de cumplimiento de 0.0 a 100.0]\n\n" +
            "::HALLAZGOS_CRITICOS:: [Lista separada por ;;]\n\n" +
            "::HALLAZGOS_MAYORES:: [Lista separada por ;;]\n\n" +
            "::HALLAZGOS_MENORES:: [Lista separada por ;;]\n\n" +
            "::RECOMENDACIONES:: [Resumen de recomendaciones]\n\n" +
            "::END_AUDITORIA::\n";

    // 4. Llamada IA
    String respuesta = ollamaChatModel.call(prompt);

    // 5. Parseo & guardado
    try {
        parseAndSaveAuditoria(respuesta, empresaId, tipo, objetivo, auditorLiderId, idsDePoliticasAEvaluar);
    } catch (Exception e) {
        System.err.println("Error crítico al parsear y guardar la AUDITORÍA de Ollama: " + e.getMessage());
        e.printStackTrace();
    }

    // 6. Log
    String preguntaLog = "Generar auditoría tipo: " + tipo + " para políticas: " + idsDePoliticasAEvaluar;
    OllamaResponse ollamaResponse = new OllamaResponse(empresaId, auditorLiderId, preguntaLog, respuesta);

    return ollamaResponseRepository.save(ollamaResponse);
}


    /**
     * Helper para parsear la salida de crearAuditoria y guardarla.
     */
    private void parseAndSaveAuditoria(String respuesta, Long empresaId, String tipo, String objetivo,
                                       Long auditorLiderId, List<String> idsDePoliticas) {

        double score = 0.0;
        try {
            String scoreStr = extractValue(respuesta, "::SCORE::", "::HALLAZGOS_CRITICOS::").trim();
            score = Double.parseDouble(scoreStr);
        } catch (NumberFormatException e) {
            System.err.println("Error al parsear el SCORE de Ollama. Usando 0.0 por defecto. Respuesta: " + respuesta);
        }

        String criticosRaw = extractValue(respuesta, "::HALLAZGOS_CRITICOS::", "::HALLAZGOS_MAYORES::");
        List<String> criticos = criticosRaw.isEmpty() ? new ArrayList<>() :
                Arrays.stream(criticosRaw.split(";;")).map(String::trim).filter(s -> !s.isEmpty()).toList();

        String mayoresRaw = extractValue(respuesta, "::HALLAZGOS_MAYORES::", "::HALLAZGOS_MENORES::");
        List<String> mayores = mayoresRaw.isEmpty() ? new ArrayList<>() :
                Arrays.stream(mayoresRaw.split(";;")).map(String::trim).filter(s -> !s.isEmpty()).toList();

        String menoresRaw = extractValue(respuesta, "::HALLAZGOS_MENORES::", "::RECOMENDACIONES::");
        List<String> menores = menoresRaw.isEmpty() ? new ArrayList<>() :
                Arrays.stream(menoresRaw.split(";;")).map(String::trim).filter(s -> !s.isEmpty()).toList();

        String recomendaciones = extractValue(respuesta, "::RECOMENDACIONES::", "::END_AUDITORIA::");

        String alcance = "Auditoría IA de políticas: " + idsDePoliticas;

        Auditoria auditoriaFinalizada = new Auditoria(
                empresaId,
                tipo,
                objetivo,
                alcance,
                auditorLiderId,
                LocalDateTime.now(),
                score,
                criticos,
                mayores,
                menores,
                recomendaciones
        );

        auditoriaRepository.save(auditoriaFinalizada);
    }

    /**
     * Helper para extraer texto entre delimitadores.
     */
    private String extractValue(String text, String startTag, String endTag) {
        try {
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