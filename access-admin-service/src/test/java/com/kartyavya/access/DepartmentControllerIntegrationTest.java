package com.kartyavya.access;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kartyavya.access.dto.DepartmentCreateRequest;
import com.kartyavya.access.dto.DepartmentUpdateRequest;
import com.kartyavya.access.entity.Role;
import com.kartyavya.access.entity.User;
import com.kartyavya.access.entity.UserRole;
import com.kartyavya.access.repository.DepartmentRepository;
import com.kartyavya.access.repository.RoleRepository;
import com.kartyavya.access.repository.UserRepository;
import com.kartyavya.access.repository.UserRoleRepository;
import com.kartyavya.access.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("integration")
class DepartmentControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtService jwtService;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private DepartmentRepository departmentRepository;

    private String adminToken;
    private String citizenToken;
    private User adminUser;
    private User citizenUser;

    private String suffix;

    @BeforeEach
    void setUp() {
        suffix = UUID.randomUUID().toString().substring(0, 8);
        
        // Setup Admin
        adminUser = new User();
        adminUser.setName("Admin");
        adminUser.setEmail("admin-" + suffix + "@kartyavya.local");
        adminUser.setPasswordHash("hash");
        adminUser.setEnabled(true);
        adminUser = userRepository.save(adminUser);

        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
        UserRole adminUr = new UserRole();
        adminUr.setUser(adminUser);
        adminUr.setRole(adminRole);
        userRoleRepository.save(adminUr);

        adminToken = jwtService.generateToken(adminUser, "ADMIN", null);

        // Setup Citizen
        citizenUser = new User();
        citizenUser.setName("Citizen");
        citizenUser.setEmail("citizen-" + suffix + "@kartyavya.local");
        citizenUser.setPasswordHash("hash");
        citizenUser.setEnabled(true);
        citizenUser = userRepository.save(citizenUser);

        Role citizenRole = roleRepository.findByName("CITIZEN").orElseThrow();
        UserRole citizenUr = new UserRole();
        citizenUr.setUser(citizenUser);
        citizenUr.setRole(citizenRole);
        userRoleRepository.save(citizenUr);

        citizenToken = jwtService.generateToken(citizenUser, "CITIZEN", null);
    }

    @AfterEach
    void tearDown() {
        departmentRepository.findAll().stream()
                .filter(d -> d.getName().endsWith(suffix))
                .forEach(departmentRepository::delete);
        
        userRoleRepository.findByUser(adminUser).ifPresent(userRoleRepository::delete);
        userRepository.delete(adminUser);
        
        userRoleRepository.findByUser(citizenUser).ifPresent(userRoleRepository::delete);
        userRepository.delete(citizenUser);
    }

    @Test
    void post_validRequest_returns201() throws Exception {
        DepartmentCreateRequest req = new DepartmentCreateRequest("Test Dept " + suffix, "test@kartyavya.local", null);

        mockMvc.perform(post("/api/departments")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Dept " + suffix))
                .andExpect(jsonPath("$.contactEmail").value("test@kartyavya.local"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void post_duplicateName_returns409() throws Exception {
        DepartmentCreateRequest req = new DepartmentCreateRequest("Test Dept " + suffix, "test@kartyavya.local", null);
        
        mockMvc.perform(post("/api/departments")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // Second POST
        mockMvc.perform(post("/api/departments")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DUPLICATE_RESOURCE"));
    }

    @Test
    void patch_singleField_returns200() throws Exception {
        DepartmentCreateRequest req = new DepartmentCreateRequest("Test Dept " + suffix, "test@kartyavya.local", null);
        String resp = mockMvc.perform(post("/api/departments")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andReturn().getResponse().getContentAsString();
        
        Long id = objectMapper.readTree(resp).get("id").asLong();

        DepartmentUpdateRequest updateReq = new DepartmentUpdateRequest("Updated " + suffix, null, null);
        mockMvc.perform(patch("/api/departments/" + id)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated " + suffix))
                .andExpect(jsonPath("$.contactEmail").value("test@kartyavya.local"));
    }

    @Test
    void patch_allNull_returns400ValidationFailed() throws Exception {
        mockMvc.perform(patch("/api/departments/1")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.request").exists());
    }

    @Test
    void patch_unknownField_returns400ValidationFailed() throws Exception {
        mockMvc.perform(patch("/api/departments/1")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Updated\", \"unknownField\": \"value\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.unknownField").exists());
    }

    @Test
    void post_withCitizenToken_returns403() throws Exception {
        DepartmentCreateRequest req = new DepartmentCreateRequest("Test Dept " + suffix, "test@kartyavya.local", null);

        mockMvc.perform(post("/api/departments")
                .header("Authorization", "Bearer " + citizenToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
