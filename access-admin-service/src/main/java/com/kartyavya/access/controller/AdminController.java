package com.kartyavya.access.controller;

import com.kartyavya.access.dto.CreateOfficerRequest;
import com.kartyavya.access.dto.OfficerResponse;
import com.kartyavya.access.dto.PageResponse;
import com.kartyavya.access.dto.UserResponse;
import com.kartyavya.access.dto.UserStatusRequest;
import com.kartyavya.access.security.AuthenticatedPrincipal;
import com.kartyavya.access.service.OfficerService;
import com.kartyavya.access.service.UserAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administration", description = "Endpoints for managing users and officers")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final UserAdminService userAdminService;
    private final OfficerService officerService;

    public AdminController(UserAdminService userAdminService, OfficerService officerService) {
        this.userAdminService = userAdminService;
        this.officerService = officerService;
    }

    @GetMapping("/users")
    @Operation(summary = "List users with optional filters (ADMIN only)")
    public PageResponse<UserResponse> listUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return userAdminService.list(role, enabled, page, size);
    }

    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Enable or disable a user account (ADMIN only)")
    public UserResponse updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusRequest req,
            Authentication authentication) {
        AuthenticatedPrincipal p = (AuthenticatedPrincipal) authentication.getPrincipal();
        return userAdminService.updateStatus(id, req.enabled(), p.userId());
    }

    @PostMapping("/officers")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new department officer (ADMIN only)")
    public UserResponse createOfficer(@Valid @RequestBody CreateOfficerRequest req) {
        return officerService.createOfficer(req);
    }

    @GetMapping("/officers")
    @Operation(summary = "List active department officers (ADMIN only)")
    public PageResponse<OfficerResponse> listOfficers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return officerService.list(page, size);
    }
}
