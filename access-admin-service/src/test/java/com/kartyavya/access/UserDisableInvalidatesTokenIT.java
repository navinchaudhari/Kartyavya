package com.kartyavya.access;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kartyavya.access.dto.UserStatusRequest;
import com.kartyavya.access.entity.Role;
import com.kartyavya.access.entity.User;
import com.kartyavya.access.entity.UserRole;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("integration")
class UserDisableInvalidatesTokenIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtService jwtService;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRoleRepository userRoleRepository;

    private String adminToken;
    private User adminUser;
    
    private String citizenToken;
    private User citizenUser;
    
    private String suffix;

    @BeforeEach
    void setUp() {
        suffix = UUID.randomUUID().toString().substring(0, 8);
        
        // Admin
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

        // Citizen
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
        userRoleRepository.findByUser(adminUser).ifPresent(userRoleRepository::delete);
        userRepository.delete(adminUser);
        
        userRoleRepository.findByUser(citizenUser).ifPresent(userRoleRepository::delete);
        userRepository.delete(citizenUser);
    }

    @Test
    void disablingUser_immediatelyBlocksExistingTokens() throws Exception {
        // 1. Verify citizen token works
        mockMvc.perform(get("/api/auth/profile")
                .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isOk());

        // 2. Admin disables the citizen account
        UserStatusRequest req = new UserStatusRequest(false);
        mockMvc.perform(patch("/api/admin/users/" + citizenUser.getId() + "/status")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        // 3. Verify citizen token is now rejected because JwtAuthenticationFilter checks DB
        mockMvc.perform(get("/api/auth/profile")
                .header("Authorization", "Bearer " + citizenToken))
                .andExpect(status().isUnauthorized());
    }
}
