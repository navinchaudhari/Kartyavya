package com.kartyavya.access.controller;

import com.kartyavya.access.dto.RoutingRuleCreateRequest;
import com.kartyavya.access.dto.RoutingRuleResponse;
import com.kartyavya.access.dto.RoutingRuleUpdateRequest;
import com.kartyavya.access.service.RoutingRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routing-rules")
@Tag(name = "Routing Rules", description = "Endpoints for managing complaint routing rules")
@SecurityRequirement(name = "bearerAuth")
public class RoutingRuleController {

    private final RoutingRuleService routingRuleService;

    public RoutingRuleController(RoutingRuleService routingRuleService) {
        this.routingRuleService = routingRuleService;
    }

    @GetMapping
    @Operation(summary = "List all routing rules (ADMIN only)")
    public List<RoutingRuleResponse> listAll() {
        return routingRuleService.listAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new routing rule (ADMIN only)")
    public RoutingRuleResponse create(@Valid @RequestBody RoutingRuleCreateRequest req) {
        return routingRuleService.create(req);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update an existing routing rule (ADMIN only)")
    public RoutingRuleResponse update(@PathVariable("id") Long id, @Valid @RequestBody RoutingRuleUpdateRequest req) {
        return routingRuleService.update(id, req);
    }
}
