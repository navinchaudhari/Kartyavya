package com.kartyavya.access.controller;

import com.kartyavya.access.dto.AuthDtos.ProfileUpdate;
import com.kartyavya.access.entity.User;
import com.kartyavya.access.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/citizen")
public class CitizenController {
    private final UserRepository users;

    public CitizenController(UserRepository users) {
        this.users = users;
    }

    private User currentUser(Authentication authentication) {
        return users.findById(Long.valueOf(authentication.getName())).orElseThrow();
    }

    @GetMapping("/profile")
    Object profile(Authentication authentication) {
        User user = currentUser(authentication);
        return Map.of(
                "userId", user.getId(),
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "mobileNumber", user.getMobileNumber(),
                "address", user.getAddress() == null ? "" : user.getAddress(),
                "role", user.getRole()
        );
    }

    @PutMapping("/profile")
    @Transactional
    Object update(@Valid @RequestBody ProfileUpdate request, Authentication authentication) {
        User user = currentUser(authentication);
        user.setFullName(request.fullName());
        user.setMobileNumber(request.mobileNumber());
        user.setAddress(request.address());
        users.save(user);
        return Map.of("message", "Profile updated successfully");
    }
}
