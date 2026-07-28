package com.kartyavya.access;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kartyavya.access.dto.CreateOfficerRequest;
import com.kartyavya.access.dto.UserStatusRequest;
import com.kartyavya.access.entity.Department;
import com.kartyavya.access.entity.Role;
import com.kartyavya.access.entity.User;
import com.kartyavya.access.entity.UserRole;
import com.kartyavya.access.repository.DepartmentRepository;
import com.kartyavya.access.repository.OfficerDepartmentAssignmentRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("integration")
class AdminControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtService jwtService;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private OfficerDepartmentAssignmentRepository assignmentRepository;

    private String adminToken;
    private User adminUser;
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
        adminUser = userRepository.save(adminUser);

        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
        UserRole adminUr = new UserRole();
        adminUr.setUser(adminUser);
        adminUr.setRole(adminRole);
        userRoleRepository.save(adminUr);
        adminToken = jwtService.generateToken(adminUser, "ADMIN", null);

        testDept = new Department();
        testDept.setName("Test Dept " + suffix);
        testDept.setContactEmail("test@kartyavya.local");
        testDept.setEnabled(true);
        testDept = departmentRepository.save(testDept);
    }

    @AfterEach
    void tearDown() {
        userRepository.findByEmail("officer-" + suffix + "@kartyavya.local").ifPresent(u -> {
            assignmentRepository.findByOfficerId(u.getId()).ifPresent(assignmentRepository::delete);
            userRoleRepository.findByUser(u).ifPresent(userRoleRepository::delete);
            userRepository.delete(u);
        });

        departmentRepository.delete(testDept);
        
        userRoleRepository.findByUser(adminUser).ifPresent(userRoleRepository::delete);
        userRepository.delete(adminUser);
    }

    @Test
    void post_officers_returns201() throws Exception {
        CreateOfficerRequest req = new CreateOfficerRequest("Officer Name", "Officer-" + suffix + "@Kartyavya.local", "securepassword", testDept.getId());

        mockMvc.perform(post("/api/admin/officers")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("officer-" + suffix + "@kartyavya.local"))
                .andExpect(jsonPath("$.role").value("DEPARTMENT_OFFICER"));
    }

    @Test
    void patch_usersStatus_selfDisable_returns400() throws Exception {
        UserStatusRequest req = new UserStatusRequest(false);

        mockMvc.perform(patch("/api/admin/users/" + adminUser.getId() + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
    }

    @Test
    void get_users_pageValidation_returns400() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .param("page", "-1")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.page").exists());
    }

    @Test
    void get_users_invalidRole_returns400() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .param("role", "INVALID_ROLE")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.role").exists());
    }
}
