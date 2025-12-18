package com.backendie.service;

import com.backendie.models.*;
import com.backendie.repository.*;
import com.backendie.multitenancy.TenantSecurity;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.ConfigurableEnvironment; // Para leer properties
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.backendie.service.MLModelService; // new: usar predicción del modelo como contexto

@Service
@RequiredArgsConstructor
public class OllamaResponseService {

    private final OllamaResponseRepository ollamaResponseRepository;

    private final UsuarioRepository usuarioRepository;

    private final EmpresaRepository empresaRepository;

    private final CategoriaIndustriaRepository categoriaIndustriaRepository;

    private final RegulacionRepository regulacionRepository;

    private final OllamaChatModel ollamaChatModel;

    private final PoliticaEmpresaRepository politicaEmpresaRepository;

    private final ProtocoloRepository protocoloRepository;

    private final ProcedimientoRepository procedimientoRepository;

    private final ConfigurableEnvironment env;

    private final AuditoriaRepository auditoriaRepository;

    private final RiesgoRepository riesgoRepository;

    private final TenantSecurity tenantSecurity;

    private final MLModelService mlModelService; // injected to get model predictions and include them in prompts

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

    public OllamaResponse crearPPP(Long empresaId, Long usuarioId, String pregunta) {
        if (empresaId == null || usuarioId == null || pregunta == null || pregunta.isBlank()) {
            throw new IllegalArgumentException("Los campos empresaId, usuarioId y pregunta son obligatorios.");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        if (!usuario.getEmpresaId().equals(empresaId)) {
            throw new IllegalArgumentException("Usuario no pertenece a la empresa.");
        }

        if (empresa.getStatus() == null || !empresa.getStatus().equalsIgnoreCase("active")) {
            throw new IllegalArgumentException("La empresa debe estar activa para generar documentación.");
        }

        CategoriaIndustria categoria = categoriaIndustriaRepository.findById(empresa.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría de industria no encontrada."));

        List<String> regulacionesIds = categoria.getRegulaciones() == null ? Collections.emptyList() : categoria.getRegulaciones();

        List<Regulacion> regulaciones = regulacionesIds.stream()
                .map(id -> regulacionRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .toList();

        StringBuilder regulacionesStr = new StringBuilder();
        for (Regulacion regulacion : regulaciones) {
            regulacionesStr.append("\n\n").append(regulacion.getContenido());
        }

        String prompt = "Actúa como un experto en consultoría de cumplimiento normativo y excelencia operativa para la industria de: " + categoria.getNombre() + ".\n" +
                "Tu tarea es generar un paquete de documentación completo para la empresa " + empresa.getNombre() + " sobre el tema: " + pregunta + ".\n\n" +
                "**Contexto Regulatorio Obligatorio (Leyes y Normas):**\n" + regulacionesStr + "\n\n" +
                "**Instrucciones de Formato de Salida:**\n" +
                "Genera UNA política, UN protocolo principal y AL MENOS UN procedimiento detallado. Usa los delimitadores EXACTOS:\n" +
                "::POLITICA:: ... ::END_POLITICA::\n::PROTOCOLO:: ... ::END_PROTOCOLO::\n::PROCEDIMIENTO:: ... ::END_PROCEDIMIENTO::\n";

        String respuesta = ollamaChatModel.call(prompt);

        try {
            parseAndSaveDocumentation(respuesta, empresaId);
        } catch (Exception e) {
            System.err.println("Error crítico al parsear y guardar la documentación de Ollama: " + e.getMessage());
            e.printStackTrace();
        }

        OllamaResponse ollamaResponse = new OllamaResponse(empresaId, usuarioId, pregunta, respuesta);
        return ollamaResponseRepository.save(ollamaResponse);
    }

    // New method: classify risk using Ollama LLM
    public Map<String, Object> classifyRisk(Long empresaId, Long usuarioId, List<String> idsDePoliticasAEvaluar) {
        // build context
        Empresa empresa = empresaRepository.findById(empresaId).orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));
        List<PoliticaEmpresa> politicas = politicaEmpresaRepository.findAllById(idsDePoliticasAEvaluar);
        StringBuilder contexto = new StringBuilder();
        for (PoliticaEmpresa p : politicas) {
            contexto.append("\n\n--- ").append(p.getTitulo()).append(" ---\n").append(p.getContenido());
        }

        // Try to get a model-based prediction to include as additional context
        String modelHint = "";
        try {
            Map<String,Object> modelOut = mlModelService.predict(contexto.toString());
            if (modelOut != null) {
                Object lbl = modelOut.getOrDefault("label", modelOut.getOrDefault("risk", null));
                Object conf = modelOut.getOrDefault("confidence", modelOut.getOrDefault("probabilities", null));
                if (lbl != null) {
                    modelHint = "Modelo_sugiere_riesgo: " + lbl.toString();
                    if (conf instanceof Number) modelHint += " (conf: " + conf.toString() + ")";
                    else if (conf instanceof List) modelHint += " (probabilidades incluidas)";
                }
            }
        } catch (Exception e) {
            // ignore model failures and continue with Ollama only; we log for trace
            System.err.println("Warning: ML model prediction failed while enriching prompt for Ollama classifyRisk: " + e.getMessage());
        }

        String prompt = "Eres un modelo que clasifica el nivel de riesgo de cumplimiento de una empresa en base a sus políticas y documentación. " +
                "Devuelve la salida usando exactamente estos delimitadores:\n" +
                "::RISK:: [Alto|Medio|Bajo]\n" +
                "::CONFIDENCE:: [0-100]\n" +
                "::EXPLANATION:: [Explicación breve]\n" +
                "Analiza el siguiente contexto:\n" + contexto + "\n" + (modelHint.isBlank() ? "" : "\nADICIONAL_DEL_MODELO:\n" + modelHint + "\n");

        String respuesta = ollamaChatModel.call(prompt);

        String risk = extractValue(respuesta, "::RISK::", "::CONFIDENCE::");
        String confidence = extractValue(respuesta, "::CONFIDENCE::", "::EXPLANATION::");
        String explanation = extractValue(respuesta, "::EXPLANATION::", null);

        // sanitize
        risk = risk.isBlank() ? "Desconocido" : risk.trim();
        double conf = 0.0;
        try { conf = Double.parseDouble(confidence.trim()); } catch (Exception ignored) {}

        // Save as Riesgo entity (registro de evaluación)
        Riesgo r = new Riesgo();
        r.setEmpresaId(empresaId);
        r.setTitulo("Evaluación de riesgo IA");
        r.setDescripcion("Clasificación automática de riesgo");
        r.setCategoria("compliance");
        r.setProbabilidad(conf > 66 ? "alta" : conf > 33 ? "media" : "baja");
        r.setImpacto("medio");
        r.setNivelRiesgo(risk);
        r.setMedidasMitigacion(explanation);
        r.setResponsable(usuarioId);
        r.setEstado("evaluado");

        riesgoRepository.save(r);

        Map<String,Object> out = new HashMap<>();
        out.put("risk", risk);
        out.put("confidence", conf);
        out.put("explanation", explanation);
        out.put("riesgoId", r.getId());
        out.put("raw", respuesta);
        return out;
    }

    // New method: NER using Ollama with JSON parsing fallback
    public Map<String, Object> extractEntities(String text) {
        String prompt = "Extrae entidades relevantes (FECHAS, ENTIDADES, MONTO, DIRECCIONES, NOMBRES) del siguiente texto y devuelve en formato JSON:\n" +
                text + "\n" +
                "Responde SOLO con un objeto JSON con keys: dates, entities, amounts, addresses, names";
        String respuesta = ollamaChatModel.call(prompt);
        Map<String,Object> out = new HashMap<>();
        out.put("raw", respuesta);

        // Try parse JSON from response
        Map<String,Object> parsed = parseJsonIfPossible(respuesta);
        if (parsed != null) {
            out.putAll(parsed);
            return out;
        }

        // Fallback heuristics: amounts and dates
        try {
            List<String> amounts = new ArrayList<>();
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\$\\s?([0-9.,]+)").matcher(respuesta);
            while (m.find()) amounts.add(m.group(1));
            List<String> dates = new ArrayList<>();
            java.util.regex.Matcher d = java.util.regex.Pattern.compile("(\\d{4}-\\d{2}-\\d{2}|\\d{2}/\\d{2}/\\d{4}|\\d{1,2} de [A-Za-z]+ de \\\\d{4})").matcher(respuesta);
            while (d.find()) dates.add(d.group());

            out.put("amounts", amounts);
            out.put("dates", dates);
        } catch (Exception e) {
            // ignore fallback errors
        }

        return out;
    }

    private Map<String,Object> parseJsonIfPossible(String text) {
        // Very forgiving JSON extraction: find first { ... } block and parse with Jackson
        try {
            int start = text.indexOf('{');
            int end = text.lastIndexOf('}');
            if (start == -1 || end == -1 || end <= start) return null;
            String json = text.substring(start, end+1);
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.findAndRegisterModules();
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    public OllamaResponse crearAuditoria(Long empresaId, String tipo, String objetivo,
                                     Long auditorLiderId, List<String> idsDePoliticasAEvaluar) {

    // Tenant check: ensure empresa belongs to current tenant category
    tenantSecurity.assertEmpresaBelongsToCurrentCategoria(empresaId);

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

        List<Protocolo> protocolos = protocoloRepository.findByPoliticaId(politica.getId()); // <- String
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
            System.err.println("Error al parsear el SCORE de Ollama. Intentando extraer número por regex. Respuesta: " + respuesta);
            // Fallback: buscar primer número entre 0 y 100 en la respuesta
            try {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{1,3}(?:\\.\\d+)?)").matcher(respuesta);
                if (m.find()) {
                    double val = Double.parseDouble(m.group(1));
                    if (val >= 0.0 && val <= 100.0) score = val;
                }
            } catch (Exception ex) {
                // ignore
            }
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

    /**
     * Helper para parsear la salida de crearPPP (document generation) y guardarla en PoliticaEmpresa/Protocolo/Procedimiento.
     */
    private void parseAndSaveDocumentation(String respuesta, Long empresaId) {
        String aiModel = env.getProperty("spring.ai.ollama.chat.model", "desconocido");

        String politicaBlock = extractValue(respuesta, "::POLITICA::", "::END_POLITICA::");
        String politicaTitulo = extractValue(politicaBlock, "::titulo::", null);
        String politicaContenido = extractValue(politicaBlock, "::contenido::", null);

        PoliticaEmpresa politica = new PoliticaEmpresa();
        politica.setEmpresaId(empresaId);
        politica.setTitulo(politicaTitulo);
        politica.setContenido(politicaContenido);
        politica.setAiGenerada(true);
        politica.setAiModeloVersion(aiModel);
        politica.setEstado("draft");

        PoliticaEmpresa politicaGuardada = politicaEmpresaRepository.save(politica);
        String politicaId = politicaGuardada.getId();

        String protocoloBlock = extractValue(respuesta, "::PROTOCOLO::", "::END_PROTOCOLO::");
        String protoNombre = extractValue(protocoloBlock, "::nombre::", null);
        String protoDesc = extractValue(protocoloBlock, "::descripcion::", null);
        String protoObjetivo = extractValue(protocoloBlock, "::objetivo::", null);
        String protoReglasRaw = extractValue(protocoloBlock, "::reglas::", null);

        List<String> protoReglas = protoReglasRaw == null || protoReglasRaw.isEmpty() ? Collections.emptyList() :
                Arrays.stream(protoReglasRaw.split(";;")).map(String::trim).toList();

        Protocolo protocolo = new Protocolo();
        protocolo.setNombre(protoNombre);
        protocolo.setDescripcion(protoDesc);
        protocolo.setEmpresaId(empresaId);
        protocolo.setObjetivo(protoObjetivo);
        protocolo.setReglas(protoReglas);
        protocolo.setPoliticaId(politicaId);

        Protocolo protocoloGuardado = protocoloRepository.save(protocolo);
        String protocoloId = protocoloGuardado.getIdProtocolo();

        // Procedimientos: find all PROCEDIMIENTO blocks
        Pattern procPattern = Pattern.compile("::PROCEDIMIENTO::(.*?)::END_PROCEDIMIENTO::", Pattern.DOTALL);
        Matcher procMatcher = procPattern.matcher(respuesta);
        while (procMatcher.find()) {
            String procBlock = procMatcher.group(1);
            String procNombre = extractValue(procBlock, "::nombre::", null);
            String procDesc = extractValue(procBlock, "::descripcion::", null);
            String procObjetivo = extractValue(procBlock, "::objetivo::", null);
            String procPasosRaw = extractValue(procBlock, "::pasos::", null);

            List<String> procPasos = procPasosRaw == null || procPasosRaw.isEmpty() ? Collections.emptyList() :
                    Arrays.stream(procPasosRaw.split(";;")).map(String::trim).toList();

            Procedimiento procedimiento = new Procedimiento(protocoloId, procPasos, procObjetivo, empresaId, procDesc, procNombre);
            procedimientoRepository.save(procedimiento);
        }
    }
}
