package com.BackendIE.BackendIE.Service;

import com.BackendIE.BackendIE.Models.*;
import com.BackendIE.BackendIE.Repository.*;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RiesgoService {

    @Autowired
    private RiesgoRepository riesgoRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PoliticaEmpresaRepository politicaEmpresaRepository;

    @Autowired
    private ProtocoloRepository protocoloRepository;

    @Autowired
    private ProcedimientoRepository procedimientoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OllamaChatModel ollamaChatModel;

    @Autowired
    private OllamaResponseRepository ollamaResponseRepository;

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
        List<PoliticaEmpresa> politicas = politicaEmpresaRepository.findAllById(idsDePoliticas);

        for (PoliticaEmpresa politica : politicas) {
            contexto.append("\n\n--- POLÍTICA: ").append(politica.getTitulo()).append(" ---\n");
            contexto.append(politica.getContenido());

            List<Protocolo> protocolos = protocoloRepository.findByIdPolitica(politica.getId());
            for (Protocolo proto : protocolos) {
                contexto.append("\n  - PROTOCOLO: ").append(proto.getNombre())
                        .append("\n  Descripción: ").append(proto.getDescripcion())
                        .append("\n  Objetivo: ").append(proto.getObjetivo());

                List<Procedimiento> procedimientos = procedimientoRepository.findByProtocoloId(proto.getId());
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

        // 3️⃣ Construcción del prompt para Ollama
        String prompt = """
                Actúa como un Analista de Riesgos experto en cumplimiento normativo y gestión empresarial.
                Tu tarea es identificar y clasificar posibles riesgos dentro de la empresa "%s",
                usando la siguiente documentación interna (políticas, protocolos y procedimientos):

                %s

                Formatea tu respuesta con los siguientes delimitadores EXACTOS:
                ::RIESGOS_OPERATIVOS:: [Lista separada por ;;]
                ::RIESGOS_FINANCIEROS:: [Lista separada por ;;]
                ::RIESGOS_ESTRATEGICOS:: [Lista separada por ;;]
                ::RIESGOS_CUMPLIMIENTO:: [Lista separada por ;;]
                ::RECOMENDACIONES_GENERALES:: [Texto]
                ::END_RIESGOS::
                """.formatted(empresa.getNombre(), contexto);

        // 4️⃣ Llamada al modelo Ollama
        String respuesta = ollamaChatModel.call(prompt);

        // 5️⃣ Parseo y guardado de los riesgos generados
        try {
            parseAndSaveRiesgos(respuesta, empresaId, usuarioId);
        } catch (Exception e) {
            System.err.println("Error crítico al parsear y guardar los riesgos generados por Ollama: " + e.getMessage());
            e.printStackTrace();
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
