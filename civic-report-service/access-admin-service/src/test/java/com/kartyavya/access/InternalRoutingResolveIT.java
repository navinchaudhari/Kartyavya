package com.kartyavya.access;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("integration")
class InternalRoutingResolveIT {

    @Autowired private MockMvc mockMvc;
    
    @Value("${internal.service-key:}")
    private String internalServiceKey;

    @Test
    void resolve_validKey_returns200() throws Exception {
        // Assuming POTHOLE is seeded
        mockMvc.perform(get("/internal/routing/resolve")
                .param("category", "POTHOLE")
                .header("X-Internal-Service-Key", internalServiceKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("POTHOLE"))
                .andExpect(jsonPath("$.departmentName").exists());
    }

    @Test
    void resolve_missingKey_returns401() throws Exception {
        mockMvc.perform(get("/internal/routing/resolve")
                .param("category", "POTHOLE"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVICE_AUTH_FAILED"));
    }

    @Test
    void resolve_wrongKey_returns401() throws Exception {
        mockMvc.perform(get("/internal/routing/resolve")
                .param("category", "POTHOLE")
                .header("X-Internal-Service-Key", "wrong-key"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INTERNAL_SERVICE_AUTH_FAILED"));
    }

    @Test
    void resolve_invalidCategory_returns400() throws Exception {
        mockMvc.perform(get("/internal/routing/resolve")
                .param("category", "INVALID_CAT")
                .header("X-Internal-Service-Key", internalServiceKey))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }
}
