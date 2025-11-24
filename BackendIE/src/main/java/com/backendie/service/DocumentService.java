package com.backendie.service;

import com.backendie.models.PoliticaEmpresa;
import com.backendie.repository.PoliticaEmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final PoliticaEmpresaRepository politicaEmpresaRepository;

    public byte[] renderPoliticaToPdf(String politicaId) {
        PoliticaEmpresa p = politicaEmpresaRepository.findById(politicaId)
                .orElseThrow(() -> new IllegalArgumentException("Política no encontrada: " + politicaId));

        String html = buildHtmlFromPolitica(p);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF: " + e.getMessage(), e);
        }
    }

    private String buildHtmlFromPolitica(PoliticaEmpresa p) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta charset=\"utf-8\"></head><body>");
        sb.append("<h1>").append(escapeHtml(p.getTitulo())).append("</h1>");
        sb.append("<p>").append(escapeHtml(p.getContenido()).replaceAll("\\n","<br/>"))
                .append("</p>");
        sb.append("<hr/>");
        sb.append("<p>Empresa ID: ").append(p.getEmpresaId()).append("</p>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
    }
}

