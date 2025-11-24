package com.backendie.integration;

import com.backendie.dtos.JwtResponse;
import com.backendie.dtos.Loginuserdto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class MultiTenantOllamaIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private JwtResponse login(String email, String password) {
        Loginuserdto dto = new Loginuserdto();
        dto.setEmail(email);
        dto.setPassword(password);
        ResponseEntity<JwtResponse> resp = restTemplate.postForEntity("/api/usuarios/login", dto, JwtResponse.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        return resp.getBody();
    }

    @Test
    public void tenantIsolationAndOllamaRequest() {
        // Login as admin A (Banco Continental)
        JwtResponse adminA = login("maria.lopez@bancocontinental.com", "password123");
        assertThat(adminA).isNotNull();
        assertThat(adminA.getToken()).isNotBlank();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminA.getToken());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // Request empresas resumen -> should return only companies in adminA's category
        ResponseEntity<List> empresasA = restTemplate.exchange("/api/empresas/resumen", HttpMethod.GET, entity, List.class);
        assertThat(empresasA.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<?> bodyA = empresasA.getBody();
        assertThat(bodyA).isNotNull();
        assertThat(bodyA.size()).isGreaterThan(0);

        // Now login as admin B (Clinica Nueva Vida)
        JwtResponse adminB = login("andres.gomez@clinicavida.com", "password123");
        assertThat(adminB).isNotNull();
        assertThat(adminB.getToken()).isNotBlank();

        HttpHeaders headersB = new HttpHeaders();
        headersB.setBearerAuth(adminB.getToken());
        HttpEntity<Void> entityB = new HttpEntity<>(headersB);

        ResponseEntity<List> empresasB = restTemplate.exchange("/api/empresas/resumen", HttpMethod.GET, entityB, List.class);
        assertThat(empresasB.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<?> bodyB = empresasB.getBody();
        assertThat(bodyB).isNotNull();
        assertThat(bodyB.size()).isGreaterThan(0);

        // The two results should not be identical (different categories)
        assertThat(bodyA).isNotEqualTo(bodyB);

        // Test Ollama Response fetch (reads from Mongo seeded data) - request as String to avoid parsing errors
        ResponseEntity<String> ollamaList = restTemplate.exchange("/api/ollama", HttpMethod.GET, entity, String.class);
        assertThat(ollamaList.getStatusCode()).isEqualTo(HttpStatus.OK);
        String ollamaBody = ollamaList.getBody();
        assertThat(ollamaBody).isNotNull();
    }
}
