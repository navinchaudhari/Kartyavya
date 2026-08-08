package com.kartyavya.access.controller;

import com.kartyavya.access.dto.*;
import com.kartyavya.access.security.AuthenticatedPrincipal;
import com.kartyavya.access.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints — frozen contract: docs/contracts/api-contracts.md §1.
 *
 * POST /api/auth/register → 201 UserResponse      (public)
 * POST /api/auth/login    → 200 LoginResponse      (public)
 * GET  /api/auth/me       → 200 UserProfileResponse (authenticated)
 *
 * The /me endpoint resolves the current user EXCLUSIVELY from the authenticated principal
 * in SecurityContextHolder. Query parameters, path variables, and request body are ignored
 * for user identity — even if a ?userId= is present in the request.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(
    name = "Authentication",
    description = "Citizen registration, login and authenticated profile APIs"
)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register a new citizen", description = "Creates a new account with the CITIZEN role and stores the password using BCrypt hashing.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "citizen registered successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "invalid name, email or password",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "email already registered",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "unexpected server error",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Operation(summary = "Authenticate a user", description = "Validates email and password and returns a JWT access token.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "login successful",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "400", description = "invalid request body",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "invalid credentials",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "unexpected server error",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Get the authenticated user profile", description = "Returns the profile of the user identified by the supplied JWT.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "authenticated profile returned",
            content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
        @ApiResponse(responseCode = "401", description = "missing, invalid or expired JWT",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "authenticated user no longer exists",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(Authentication authentication) {
        // Principal is set exclusively by JwtAuthenticationFilter from the validated JWT.
        // Any spoofed query param (e.g. ?userId=...) is deliberately ignored here.
        AuthenticatedPrincipal principal = (AuthenticatedPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(authService.getProfile(principal.userId()));
    }
}
