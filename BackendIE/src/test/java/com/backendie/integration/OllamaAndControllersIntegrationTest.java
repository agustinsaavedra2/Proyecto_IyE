package com.backendie.integration;

import com.backendie.models.Empresa;
import com.backendie.repository.AuditoriaRepository;
import com.backendie.repository.EmpresaRepository;
import com.backendie.repository.OllamaResponseRepository;
import com.backendie.repository.PoliticaEmpresaRepository;
import com.backendie.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.ai.ollama.OllamaChatModel;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OllamaAndControllersIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    Environment env;

    @Autowired
    EmpresaRepository empresaRepository;

    @Autowired
    PoliticaEmpresaRepository politicaEmpresaRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    OllamaResponseRepository ollamaResponseRepository;

    @Autowired
    AuditoriaRepository auditoriaRepository;

    @Autowired
    private OllamaChatModel ollamaChatModel;

    private String ollamaBase;
    private String tenantHeader;

    @BeforeEach
    void setup() {
        ollamaBase = env.getProperty("spring.ai.ollama.base-url", "http://localhost:11434");
        tenantHeader = env.getProperty("app.multitenancy.header", "X-Categoria-id");
        // Use real OllamaChatModel (will call your local Ollama). The test will be skipped if Ollama is not reachable.
        // Wait for DatabaseSeeder to populate data (max 30s)
        waitForSeed(30_000);
    }

    private void waitForSeed(long timeoutMillis) {
        long start = System.currentTimeMillis();
        long sleep = 500;
        try {
            while (System.currentTimeMillis() - start < timeoutMillis) {
                long emp = empresaRepository.count();
                long pol = politicaEmpresaRepository.count();
                long usr = usuarioRepository.count();
                if (emp > 0 && pol > 0 && usr > 0) {
                    return;
                }
                Thread.sleep(sleep);
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        // If we are here, seeding didn't complete in time
        throw new IllegalStateException("Database does not contain seeded data after waiting " + timeoutMillis/1000 + "s. " +
                "Check that your Postgres/Mongo credentials (DB_PASSWORD, DB_POSTGRES_*, DB_MONGO_*) are correct and that the application can connect. " +
                "You can run the app once (mvn spring-boot:run) to let DatabaseSeeder populate the DB.");
    }

    private boolean isHostReachable(String baseUrl) {
        try {
            java.net.URI uri = java.net.URI.create(baseUrl);
            String host = uri.getHost();
            int port = uri.getPort();
            if (port == -1) port = "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 800);
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    // Helper to login and obtain JWT token from /api/usuarios/login
    private String obtainAccessToken(String email, String password) throws Exception {
        Map<String, String> login = Map.of("email", email, "password", password);
        var result = mockMvc.perform(post("/api/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        Map<String, Object> map = objectMapper.readValue(body, Map.class);
        Object token = map.get("token");
        return token != null ? token.toString() : null;
    }

    @Test
    public void testCrearAuditoria_viaController_and_AI() throws Exception {
        // Verifica que haya datos seeded en la BD
        Assumptions.assumeTrue(empresaRepository.count() > 0, "No hay empresas en la BD. Ejecuta el DatabaseSeeder o habilita la BD de pruebas.");
        Assumptions.assumeTrue(politicaEmpresaRepository.count() > 0, "No hay políticas en la BD. Ejecuta el DatabaseSeeder o inserta datos de prueba.");
        Assumptions.assumeTrue(usuarioRepository.count() > 0, "No hay usuarios en la BD. Ejecuta el DatabaseSeeder o inserta datos de prueba.");

        // Verifica que Ollama esté disponible; si no, se asume la prueba
        Assumptions.assumeTrue(isHostReachable(ollamaBase), "Ollama no está disponible en " + ollamaBase + "; prueba saltada.");

        // Use first empresa/user (Banco Continental - maria.lopez)
        Empresa empresa1 = empresaRepository.findAll().iterator().next();
        Long empresa1Id = empresa1.getId();
        Long auditorId = usuarioRepository.findAll().iterator().next().getId();
        List<String> politicasIds = politicaEmpresaRepository.findAll().stream().map(p -> p.getId()).toList();

        Map<String, Object> payload = Map.of(
                "empresaId", empresa1Id,
                "tipo", "Interno",
                "objetivo", "Evaluación rápida",
                "auditorLiderId", auditorId,
                "idsDePoliticasAEvaluar", politicasIds
        );

        long beforeCount = ollamaResponseRepository.count();

        // Obtain JWT for seeded user (password in DatabaseSeeder is "password123")
        String token1 = obtainAccessToken("maria.lopez@bancocontinental.com", "password123");
        assertNotNull(token1, "No se obtuvo token para maria.lopez");

        var result = mockMvc.perform(post("/api/ollama/crearAuditoria")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(tenantHeader, String.valueOf(empresa1Id))
                        .header("Authorization", "Bearer " + token1)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().string(not(emptyString())))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertNotNull(body);
        assertTrue(body.length() > 10, "Respuesta de la API demasiado corta.");

        // Verifica que se haya guardado una respuesta de Ollama
        long afterCount = ollamaResponseRepository.count();
        assertTrue(afterCount > beforeCount, "No se guardó la respuesta de Ollama en la BD (ollamaResponseRepository)." );

        // Verifica que se haya generado una auditoría en la tabla correspondiente
        assertTrue(auditoriaRepository.count() > 0, "La auditoría no se persistió en la BD.");

        // Now repeat for second empresa/user (Clínica Nueva Vida - andres.gomez)
        Empresa empresa2 = empresaRepository.findAll().stream().skip(1).findFirst().orElse(null);
        Assumptions.assumeTrue(empresa2 != null, "No hay segunda empresa seed para probar multi-tenant");
        Long empresa2Id = empresa2.getId();

        String token2 = obtainAccessToken("andres.gomez@clinicavida.com", "password123");
        assertNotNull(token2, "No se obtuvo token para andres.gomez");

        long beforeOllama2 = ollamaResponseRepository.count();
        long beforeAuditoria2 = auditoriaRepository.count();

        var result2 = mockMvc.perform(post("/api/ollama/crearAuditoria")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(tenantHeader, String.valueOf(empresa2Id))
                        .header("Authorization", "Bearer " + token2)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().string(not(emptyString())))
                .andReturn();

        String body2 = result2.getResponse().getContentAsString();
        assertNotNull(body2);
        assertTrue(body2.length() > 10, "Respuesta de la API demasiado corta para la segunda empresa.");

        long afterOllama2 = ollamaResponseRepository.count();
        long afterAuditoria2 = auditoriaRepository.count();

        assertTrue(afterOllama2 > beforeOllama2, "No se guardó la respuesta de Ollama para la segunda empresa");
        assertTrue(afterAuditoria2 > beforeAuditoria2, "No se persistió la auditoría para la segunda empresa");
    }

    @Test
    public void testOllama_directPing() throws Exception {
        Assumptions.assumeTrue(isHostReachable(ollamaBase), "Ollama no está disponible en " + ollamaBase + "; prueba saltada.");

        // Hacemos una llamada simple para comprobar que responde (no usamos SDK)
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        String endpoint = ollamaBase.endsWith("/") ? ollamaBase + "v1/models" : ollamaBase + "/v1/models";
        java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(endpoint))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        java.net.http.HttpResponse<String> resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
        assertTrue(resp.statusCode() >= 200 && resp.statusCode() < 300, "Ollama respondió con código no-2xx: " + resp.statusCode());
        assertNotNull(resp.body());
        assertTrue(resp.body().length() > 5);
    }
}
