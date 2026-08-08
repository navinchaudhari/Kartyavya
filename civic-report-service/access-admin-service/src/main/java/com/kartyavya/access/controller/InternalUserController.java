package com.kartyavya.access.controller;

import com.kartyavya.access.dto.UserContactResponse;
import com.kartyavya.access.entity.User;
import com.kartyavya.access.exception.ResourceNotFoundException;
import com.kartyavya.access.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/users")
@Tag(name = "Internal", description = "Internal service-to-service APIs")
@SecurityRequirement(name = "internalServiceKey")
public class InternalUserController {

    private final UserRepository userRepository;

    public InternalUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping(value = "/{userId}/contact", produces = "application/json")
    @Operation(summary = "Get user contact details (INTERNAL_SERVICE only)")
    public UserContactResponse getUserContact(@PathVariable("userId") @Positive Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return new UserContactResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.isEnabled()
        );
    }
}
