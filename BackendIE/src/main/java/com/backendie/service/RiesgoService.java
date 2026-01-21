package com.backendie.service;

import com.backendie.models.*;
import com.backendie.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class RiesgoService {

    private final RiesgoRepository riesgoRepository;

    private final EmpresaRepository empresaRepository;

    private final PoliticaEmpresaRepository politicaEmpresaRepository;

    private final ProtocoloRepository protocoloRepository;

    private final ProcedimientoRepository procedimientoRepository;

    private final UsuarioRepository usuarioRepository;

    private final OllamaChatModel ollamaChatModel;

    private final OllamaResponseRepository ollamaResponseRepository;

    // CRUD básico
    public Riesgo save(Riesgo riesgo) {
        return riesgoRepository.save(riesgo);
    }

    public List<Riesgo> findAll() {
        return riesgoRepository.findAll();
    }

    public Riesgo findById(String id) {
        return riesgoRepository.findById(id).orElse(null);
    }

    public void deleteById(String id) {
        riesgoRepository.deleteById(id);
    }

    /**
     * Método principal: genera riesgos automáticamente con Ollama
     */
    public OllamaResponse generarRiesgos(Long empresaId, Long usuarioId, List<Long> idsDePoliticas) {

        // 1️⃣ Validaciones
        if (empresaId == null || usuarioId == null || idsDePoliticas == null || idsDePoliticas.isEmpty()) {
            throw new IllegalArgumentException("Debe indicar empresa, usuario y al menos una política.");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada."));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        if (!usuario.getEmpresaId().equals(empresaId)) {
            throw new IllegalArgumentException("El usuario no pertenece a la empresa indicada.");
        }

        // 2️⃣ Recolección de documentación de contexto
        StringBuilder contexto = new StringBuilder();

        // ✅ Convertir los IDs Long → String, ya que Mongo usa String como identificador
        List<String> idsPoliticasStr = idsDePoliticas.stream()
                .map(String::valueOf)
                .toList();

        List<PoliticaEmpresa> politicas = politicaEmpresaRepository.findAllById(idsPoliticasStr);

        for (PoliticaEmpresa politica : politicas) {
            contexto.append("\n\n--- POLÍTICA: ").append(politica.getTitulo()).append(" ---\n");
            contexto.append(politica.getContenido());

            List<Protocolo> protocolos = protocoloRepository.findByPoliticaId(politica.getId());
            for (Protocolo proto : protocolos) {
                contexto.append("\n  - PROTOCOLO: ").append(proto.getNombre())
                        .append("\n  Descripción: ").append(proto.getDescripcion())
                        .append("\n  Objetivo: ").append(proto.getObjetivo());

                List<Procedimiento> procedimientos = procedimientoRepository.findByProtocoloId(proto.getIdProtocolo());
                for (Procedimiento proc : procedimientos) {
                    contexto.append("\n    * PROCEDIMIENTO: ").append(proc.getNombre())
                            .append("\n      Descripción: ").append(proc.getDescripcion())
                            .append("\n      Objetivo: ").append(proc.getObjetivo())
                            .append("\n      Pasos: ").append(String.join(";; ", proc.getPasos()));
                }
            }
        }

        if (contexto.isEmpty()) {
            throw new IllegalArgumentException("No se encontró documentación asociada a las políticas seleccionadas.");
        }

        // 3️⃣ Construcción del prompt para Ollama - pedir JSON estructurado con scores
        String prompt = String.format(
                "Actúa como un Analista de Riesgos experto en cumplimiento normativo y gestión empresarial.\n" +
                "Tu tarea es identificar y clasificar posibles riesgos dentro de la empresa '%s', usando la siguiente documentación interna (políticas, protocolos y procedimientos):\n\n%s\n\n" +
                "Devuélveme un JSON válido con la estructura: {\"riesgos\": [ { \"categoria\": \"OPERATIVO\", \"texto\": \"...\", \"score\": 0.75 }, ... ], \"recomendaciones\": \"texto\" }\n" +
                "Los scores deben estar en el rango 0.0 a 1.0 (0 = sin riesgo, 1 = riesgo máximo). Responde sólo con el JSON (sin texto adicional).",
                empresa.getNombre(), contexto.toString());

        // 4️⃣ Llamada al modelo Ollama
        String respuesta = ollamaChatModel.call(prompt);

        // 5️⃣ Intentar parsear como JSON y guardar riesgos con score. Si falla, fallback al parseo por etiquetas.
        ObjectMapper mapper = new ObjectMapper();
        boolean parsedJson = false;
        try {
            JsonNode root = mapper.readTree(respuesta);
            if (root.has("riesgos") && root.get("riesgos").isArray()) {
                for (JsonNode node : root.get("riesgos")) {
                    String categoria = node.has("categoria") ? node.get("categoria").asText() : "operativo";
                    String texto = node.has("texto") ? node.get("texto").asText() : "";
                    double score = node.has("score") ? node.get("score").asDouble() : 0.0;

                    String nivel;
                    if (score >= 0.7) nivel = "alto";
                    else if (score >= 0.4) nivel = "medio";
                    else nivel = "bajo";

                    Riesgo r = new Riesgo(
                            empresaId,
                            texto.length() > 50 ? texto.substring(0, 50) + "..." : texto,
                            texto,
                            categoria.toLowerCase(),
                            "media",
                            "medio",
                            nivel,
                            "Evaluar e implementar medidas preventivas.",
                            usuarioId,
                            "abierto"
                    );
                    r.setCreatedAt(LocalDateTime.now());
                    riesgoRepository.save(r);
                }
                parsedJson = true;
            }
        } catch (Exception e) {
            // Ignore JSON parse errors and fallback
            parsedJson = false;
        }

        if (!parsedJson) {
            // Fallback: parseo por secciones antiguas
            try {
                parseAndSaveRiesgos(respuesta, empresaId, usuarioId);
            } catch (Exception e) {
                System.err.println("Error crítico al parsear y guardar los riesgos generados por Ollama: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // 6️⃣ Registro de la interacción (log)
        String preguntaLog = "Generación de riesgos automáticos para empresa " + empresa.getNombre();
        OllamaResponse ollamaResponse = new OllamaResponse(empresaId, usuarioId, preguntaLog, respuesta);

        return ollamaResponseRepository.save(ollamaResponse);
    }

    /**
     * Helper: parsea los bloques ::RIESGOS_...:: y los guarda en MongoDB
     */
    private void parseAndSaveRiesgos(String respuesta, Long empresaId, Long responsableId) {
        Map<String, String> secciones = Map.of(
                "OPERATIVO", extractValue(respuesta, "::RIESGOS_OPERATIVOS::", "::RIESGOS_FINANCIEROS::"),
                "FINANCIERO", extractValue(respuesta, "::RIESGOS_FINANCIEROS::", "::RIESGOS_ESTRATEGICOS::"),
                "ESTRATEGICO", extractValue(respuesta, "::RIESGOS_ESTRATEGICOS::", "::RIESGOS_CUMPLIMIENTO::"),
                "CUMPLIMIENTO", extractValue(respuesta, "::RIESGOS_CUMPLIMIENTO::", "::RECOMENDACIONES_GENERALES::")
        );

        for (Map.Entry<String, String> entry : secciones.entrySet()) {
            String categoria = entry.getKey();
            String bloque = entry.getValue();

            List<String> riesgos = Arrays.stream(bloque.split(";;"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            for (String texto : riesgos) {
                Riesgo r = new Riesgo(
                        empresaId,
                        texto.length() > 50 ? texto.substring(0, 50) + "..." : texto,
                        texto,
                        categoria.toLowerCase(),
                        "media",
                        "medio",
                        "medio",
                        "Evaluar e implementar medidas preventivas.",
                        responsableId,
                        "abierto"
                );
                r.setCreatedAt(LocalDateTime.now());
                riesgoRepository.save(r);
            }
        }
    }

    private String extractValue(String text, String startTag, String endTag) {
        try {
            int startIndex = text.indexOf(startTag);
            if (startIndex == -1) return "";
            startIndex += startTag.length();
            int endIndex = text.indexOf(endTag, startIndex);
            if (endIndex == -1) endIndex = text.length();
            return text.substring(startIndex, endIndex).trim();
        } catch (Exception e) {
            return "";
        }
    }
}
