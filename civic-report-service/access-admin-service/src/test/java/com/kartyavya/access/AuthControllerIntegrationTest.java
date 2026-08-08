package com.kartyavya.access;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.
 *
 * Rules (see implementation_plan.md §Final Test Strategy):
 *   - Targets access_db (same as dev) — Config Server supplies all configuration.
 *   - No @ActiveProfiles, no application-test.properties.
 *   - Eureka disabled via @SpringBootTest properties so tests work without service registry.
 *   - Each test creates users with a unique UUID email; @AfterEach deletes only those rows.
 *   - Seeded roles, departments, and all other dev data are preserved.
 *   - Every test is fully independent — no shared state, no ordering dependency.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
    }
)
@AutoConfigureMockMvc
@Tag("integration")
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

    /** Emails of users created by this test class — cleaned up after each test */
    private final Set<String> testEmails = new LinkedHashSet<>();

    @AfterEach
    void cleanupTestUsers() {
        for (String email : testEmails) {
            // Delete user_roles first (FK constraint), then the user
            jdbcTemplate.update(
                "DELETE ur FROM user_roles ur " +
                "JOIN users u ON ur.user_id = u.id WHERE u.email = ?", email);
            jdbcTemplate.update("DELETE FROM users WHERE email = ?", email);
        }
        testEmails.clear();
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    /** Creates a unique email and registers it for cleanup. */
    private String uniqueEmail() {
        String email = "it-" + UUID.randomUUID() + "@kartyavya-test.local";
        testEmails.add(email);
        return email;
    }

    private Map<String, String> registerBody(String email) {
        return Map.of("name", "IT Test User", "email", email, "password", "Test@1234");
    }

    /** Registers a new user and returns the email (already queued for cleanup). */
    private String registerUser() throws Exception {
        String email = uniqueEmail();
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerBody(email))))
            .andExpect(status().isCreated());
        return email;
    }

    /** Logs in and extracts the token. */
    private String loginAndGetToken(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    Map.of("email", email, "password", "Test@1234"))))
            .andExpect(status().isOk())
            .andReturn();

        Map<?, ?> body = objectMapper.readValue(
            result.getResponse().getContentAsString(), Map.class);
        return (String) body.get("token");
    }

    // ── Tests ────────────────────────────────────────────────────────────────

    @Test
    void register_validBody_returns201WithUserResponse() throws Exception {
        String email = uniqueEmail();
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerBody(email))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.role").value("CITIZEN"))
            .andExpect(jsonPath("$.enabled").value(true))
            .andExpect(jsonPath("$.createdAt").isString())
            .andExpect(header().exists("X-Correlation-Id"));
    }

    @Test
    void register_duplicateEmail_returns409WithNullFieldErrors() throws Exception {
        String email = registerUser(); // first registration succeeds
        String clientCorrelationId = UUID.randomUUID().toString();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Correlation-Id", clientCorrelationId)
                .content(objectMapper.writeValueAsString(registerBody(email))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("DUPLICATE_RESOURCE"))
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.fieldErrors").value((Object) null))
            .andExpect(jsonPath("$.correlationId").value(clientCorrelationId))
            .andExpect(header().string("X-Correlation-Id", clientCorrelationId));
    }

    @Test
    void register_invalidPassword_returns400WithFieldErrors() throws Exception {
        // Use a raw email — not queued for cleanup because registration will fail
        String email = "it-invalid-" + UUID.randomUUID() + "@kartyavya-test.local";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    Map.of("name", "Test", "email", email, "password", "weak"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
            .andExpect(jsonPath("$.fieldErrors").isMap())
            .andExpect(header().exists("X-Correlation-Id"));
    }

    @Test
    void login_correctCredentials_returns200WithValidJwtAndUserSummary() throws Exception {
        String email = registerUser();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    Map.of("email", email, "password", "Test@1234"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isString())
            .andExpect(jsonPath("$.expiresAt").isString())
            .andExpect(jsonPath("$.user.email").value(email))
            .andExpect(jsonPath("$.user.role").value("CITIZEN"))
            .andExpect(jsonPath("$.user.departmentId").value((Object) null))
            .andExpect(header().exists("X-Correlation-Id"));
    }

    @Test
    void login_wrongPassword_returns401InvalidCredentials() throws Exception {
        String email = registerUser();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    Map.of("email", email, "password", "WrongPass@9999"))))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"))
            .andExpect(jsonPath("$.fieldErrors").value((Object) null))
            .andExpect(header().exists("X-Correlation-Id"));
    }

    @Test
    void me_withValidToken_returns200AndReflectsTokenOwner_spoofedQueryParamIgnored() throws Exception {
        String email = registerUser();
        String token = loginAndGetToken(email);

        // Even with a spoofed ?userId=99999, the response must reflect the token-owner
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token)
                .param("userId", "99999"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.role").value("CITIZEN"))
            .andExpect(jsonPath("$.id").value(not(99999)))
            .andExpect(header().exists("X-Correlation-Id"));
    }

    @Test
    void me_withNoToken_returns401AuthenticationRequired() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("AUTHENTICATION_REQUIRED"))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(header().exists("X-Correlation-Id"));
    }

    @Test
    void correlationId_sentByClient_isEchoedExactlyOnResponse() throws Exception {
        String clientCorrelationId = UUID.randomUUID().toString();

        // Use a request that will fail (no such user) — we only care about the header
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Correlation-Id", clientCorrelationId)
                .content(objectMapper.writeValueAsString(
                    Map.of("email", "nobody@kartyavya-test.local", "password", "Test@1234"))))
            .andExpect(header().string("X-Correlation-Id", clientCorrelationId));
    }

    @Test
    void correlationId_absentOnRequest_generatedAndReturnedOnResponse() throws Exception {
        // No X-Correlation-Id sent — server must generate a UUID and return it
        MvcResult result = mockMvc.perform(get("/api/auth/me"))
            .andExpect(header().exists("X-Correlation-Id"))
            .andReturn();

        String generatedId = result.getResponse().getHeader("X-Correlation-Id");
        assertThat(generatedId).isNotBlank();
        assertThat(UUID.fromString(generatedId)).isNotNull(); // valid UUID
    }

    // ── Security Regression Tests ────────────────────────────────────────────

    @Test
    void swaggerUi_accessibleWithoutJwt() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
            .andExpect(status().is3xxRedirection()); // Redirects to /swagger-ui/index.html
            
        mockMvc.perform(get("/swagger-ui/index.html"))
            .andExpect(status().isOk());
    }

    @Test
    void openApiDocs_accessibleWithoutJwt() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk());
    }

    @Test
    void me_endpoint_stillReturns401WithoutJwt() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized());
    }
}
