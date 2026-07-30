package com.kartyavya.access;

import com.kartyavya.access.entity.Role;
import com.kartyavya.access.entity.User;
import com.kartyavya.access.entity.UserRole;
import com.kartyavya.access.repository.RoleRepository;
import com.kartyavya.access.repository.UserRepository;
import com.kartyavya.access.repository.UserRoleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("integration")
class InternalUserContactIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private TransactionTemplate transactionTemplate;

    @Value("${internal.service-key:}")
    private String internalServiceKey;

    private User validUser;
    private User disabledUser;
    private String suffix;

    @BeforeEach
    void setUp() {
        suffix = UUID.randomUUID().toString().substring(0, 8);
        
        validUser = new User();
        validUser.setName("Valid User");
        validUser.setEmail("valid-" + suffix + "@kartyavya.local");
        validUser.setPasswordHash("hash");
        validUser.setEnabled(true);

        disabledUser = new User();
        disabledUser.setName("Disabled User");
        disabledUser.setEmail("disabled-" + suffix + "@kartyavya.local");
        disabledUser.setPasswordHash("hash");
        disabledUser.setEnabled(false);

        transactionTemplate.executeWithoutResult(status -> {
            validUser = userRepository.save(validUser);
            Role citizenRole = roleRepository.findByName("CITIZEN").orElseThrow();
            
            UserRole validUr = new UserRole();
            validUr.setUser(validUser);
            validUr.setRole(citizenRole);
            userRoleRepository.save(validUr);

            disabledUser = userRepository.save(disabledUser);
            UserRole disabledUr = new UserRole();
            disabledUr.setUser(disabledUser);
            disabledUr.setRole(citizenRole);
            userRoleRepository.save(disabledUr);
        });
    }

    @AfterEach
    void tearDown() {
        if (validUser != null && validUser.getId() != null) {
            userRoleRepository.findByUser(validUser).ifPresent(userRoleRepository::delete);
            userRepository.findById(validUser.getId()).ifPresent(userRepository::delete);
        }
        if (disabledUser != null && disabledUser.getId() != null) {
            userRoleRepository.findByUser(disabledUser).ifPresent(userRoleRepository::delete);
            userRepository.findById(disabledUser.getId()).ifPresent(userRepository::delete);
        }
    }

    @Test
    void getContact_validId_returns200() throws Exception {
        mockMvc.perform(get("/internal/users/" + validUser.getId() + "/contact")
                .header("X-Internal-Service-Key", internalServiceKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(validUser.getId()))
                .andExpect(jsonPath("$.name").value("Valid User"))
                .andExpect(jsonPath("$.email").value("valid-" + suffix + "@kartyavya.local"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void getContact_disabledUser_returns200() throws Exception {
        mockMvc.perform(get("/internal/users/" + disabledUser.getId() + "/contact")
                .header("X-Internal-Service-Key", internalServiceKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(disabledUser.getId()))
                .andExpect(jsonPath("$.name").value("Disabled User"))
                .andExpect(jsonPath("$.email").value("disabled-" + suffix + "@kartyavya.local"))
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    void getContact_missingId_returns404() throws Exception {
        // Find a dynamically missing positive user ID
        long missingId = 999999L;
        while (userRepository.existsById(missingId)) {
            missingId++;
        }
        
        mockMvc.perform(get("/internal/users/" + missingId + "/contact")
                .header("X-Internal-Service-Key", internalServiceKey))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void getContact_negativeId_returns400() throws Exception {
        mockMvc.perform(get("/internal/users/-1/contact")
                .header("X-Internal-Service-Key", internalServiceKey))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.userId").exists());
    }
}
