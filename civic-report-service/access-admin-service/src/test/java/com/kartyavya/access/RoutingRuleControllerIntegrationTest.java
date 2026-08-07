package com.kartyavya.access;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kartyavya.access.dto.RoutingRuleCreateRequest;
import com.kartyavya.access.entity.Department;
import com.kartyavya.access.entity.Role;
import com.kartyavya.access.entity.RoutingRule;
import com.kartyavya.access.entity.User;
import com.kartyavya.access.entity.UserRole;
import com.kartyavya.access.repository.DepartmentRepository;
import com.kartyavya.access.repository.RoleRepository;
import com.kartyavya.access.repository.RoutingRuleRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("integration")
class RoutingRuleControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtService jwtService;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private RoutingRuleRepository routingRuleRepository;
    @Autowired private org.springframework.transaction.support.TransactionTemplate transactionTemplate;

    private String adminToken;
    private String citizenToken;
    private User adminUser;
    private User citizenUser;
    private Department testDept;
    private String suffix;

    @BeforeEach
    void setUp() {
        suffix = UUID.randomUUID().toString().substring(0, 8);
        
        adminUser = new User();
        adminUser.setName("Admin");
        adminUser.setEmail("admin-" + suffix + "@kartyavya.local");
        adminUser.setPasswordHash("hash");
        adminUser.setEnabled(true);

        citizenUser = new User();
        citizenUser.setName("Citizen");
        citizenUser.setEmail("citizen-" + suffix + "@kartyavya.local");
        citizenUser.setPasswordHash("hash");
        citizenUser.setEnabled(true);

        transactionTemplate.executeWithoutResult(status -> {
            adminUser = userRepository.save(adminUser);
            Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
            UserRole adminUr = new UserRole();
            adminUr.setUser(adminUser);
            adminUr.setRole(adminRole);
            userRoleRepository.save(adminUr);

            citizenUser = userRepository.save(citizenUser);
            Role citizenRole = roleRepository.findByName("CITIZEN").orElseThrow();
            UserRole citizenUr = new UserRole();
            citizenUr.setUser(citizenUser);
            citizenUr.setRole(citizenRole);
            userRoleRepository.save(citizenUr);
        });

        adminToken = jwtService.generateToken(adminUser, "ADMIN", null);
        citizenToken = jwtService.generateToken(citizenUser, "CITIZEN", null);

        testDept = new Department();
        testDept.setName("Test Dept " + suffix);
        testDept.setContactEmail("test@kartyavya.local");
        testDept.setEnabled(true);
        testDept = departmentRepository.save(testDept);
    }

    @AfterEach
    void tearDown() {
        routingRuleRepository.findAll().stream()
                .filter(r -> r.getDepartment().getId().equals(testDept.getId()))
                .forEach(routingRuleRepository::delete);
        
        departmentRepository.delete(testDept);
        
        userRoleRepository.findByUser(adminUser).ifPresent(userRoleRepository::delete);
        userRepository.delete(adminUser);
        
        userRoleRepository.findByUser(citizenUser).ifPresent(userRoleRepository::delete);
        userRepository.delete(citizenUser);
    }

    @Autowired private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Test
    void post_validRequest_returns201() throws Exception {
        jdbcTemplate.update("DELETE FROM routing_rules WHERE category = 'POTHOLE'");

        try {
            RoutingRuleCreateRequest req = new RoutingRuleCreateRequest("POTHOLE", testDept.getId(), null);

            mockMvc.perform(post("/api/routing-rules")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.category").value("POTHOLE"))
                    .andExpect(jsonPath("$.departmentId").value(testDept.getId()));
        } finally {
            jdbcTemplate.update("DELETE FROM routing_rules WHERE category = 'POTHOLE'");
            jdbcTemplate.update("INSERT INTO routing_rules (category, department_id, active, created_at, updated_at) " +
                                "SELECT 'POTHOLE', id, true, NOW(6), NOW(6) FROM departments WHERE name = 'Roads & Infrastructure' LIMIT 1");
        }
    }

    @Test
    void patch_categoryFieldPresent_returns400ValidationFailed() throws Exception {
        // Supplying category is forbidden due to unknown properties failure
        mockMvc.perform(patch("/api/routing-rules/1")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\": \"GARBAGE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.category").exists());
    }

    @Test
    void post_disabledDepartment_returns400() throws Exception {
        testDept.setEnabled(false);
        departmentRepository.save(testDept);

        RoutingRuleCreateRequest req = new RoutingRuleCreateRequest("POTHOLE", testDept.getId(), true);

        mockMvc.perform(post("/api/routing-rules")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
        
        testDept.setEnabled(true);
        departmentRepository.save(testDept);
    }

    @Test
    void get_withCitizenToken_returns403() throws Exception {
        mockMvc.perform(get("/api/routing-rules")
                .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isForbidden());
    }
}
