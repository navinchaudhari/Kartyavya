package com.kartyavya.access;

import org.junit.jupiter.api.Tag;
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
@Tag("integration")
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
            .andExpect(jsonPath("$.paths['/api/auth/me'].get.security[0].bearerAuth").exists())
            // Day 2+3: new paths
            .andExpect(jsonPath("$.paths['/api/departments']").exists())
            .andExpect(jsonPath("$.paths['/api/routing-rules']").exists())
            .andExpect(jsonPath("$.paths['/api/admin/users']").exists())
            .andExpect(jsonPath("$.paths['/api/admin/officers']").exists())
            .andExpect(jsonPath("$.paths['/internal/routing/resolve']").exists())
            // Day 4: internal user contact endpoint and detailed schema assertions
            .andExpect(jsonPath("$.paths['/internal/users/{userId}/contact']").exists())
            .andExpect(jsonPath("$.paths['/internal/users/{userId}/contact'].get.parameters[0].name").value("userId"))
            .andExpect(jsonPath("$.paths['/internal/users/{userId}/contact'].get.parameters[0].in").value("path"))
            .andExpect(jsonPath("$.paths['/internal/users/{userId}/contact'].get.responses.200").exists())
            .andExpect(jsonPath("$.paths['/internal/users/{userId}/contact'].get.responses.200.content['application/json'].schema.$ref").exists())
            .andExpect(jsonPath("$.components.schemas.UserContactResponse").exists())
            .andExpect(jsonPath("$.components.schemas.UserContactResponse.properties.id").exists())
            .andExpect(jsonPath("$.components.schemas.UserContactResponse.properties.email").exists())
            // Day 2+3: internalServiceKey scheme
            .andExpect(jsonPath("$.components.securitySchemes.internalServiceKey").exists())
            .andExpect(jsonPath("$.components.securitySchemes.internalServiceKey.type").value("apiKey"))
            .andExpect(jsonPath("$.components.securitySchemes.internalServiceKey.name").value("X-Internal-Service-Key"));
    }
}
