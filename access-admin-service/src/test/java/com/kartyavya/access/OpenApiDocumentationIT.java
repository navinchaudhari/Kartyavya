package com.kartyavya.access;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
    }
)
@AutoConfigureMockMvc
class OpenApiDocumentationIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void openApi_jsonEndpoint_returnsValidContract() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.info.title").value("Kartyavya Access & Administration Service API"))
            .andExpect(jsonPath("$.info.version").value("1.0.0"))
            .andExpect(jsonPath("$.paths['/api/auth/register']").exists())
            .andExpect(jsonPath("$.paths['/api/auth/login']").exists())
            .andExpect(jsonPath("$.paths['/api/auth/me']").exists())
            .andExpect(jsonPath("$.components.securitySchemes.bearerAuth").exists())
            .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
            .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
            .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"))
            .andExpect(jsonPath("$.paths['/api/auth/me'].get.security[0].bearerAuth").exists());
    }
}
