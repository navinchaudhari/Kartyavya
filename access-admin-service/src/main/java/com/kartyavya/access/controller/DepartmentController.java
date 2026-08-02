package com.kartyavya.access.controller;

import com.kartyavya.access.dto.DepartmentCreateRequest;
import com.kartyavya.access.dto.DepartmentResponse;
import com.kartyavya.access.dto.DepartmentUpdateRequest;
import com.kartyavya.access.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@Tag(name = "Departments", description = "Endpoints for department management")
@SecurityRequirement(name = "bearerAuth")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    @Operation(summary = "List all departments (authenticated)")
    public List<DepartmentResponse> listAll() {
        return departmentService.listAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new department (ADMIN only)")
    public DepartmentResponse create(@Valid @RequestBody DepartmentCreateRequest req) {
        return departmentService.create(req);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update an existing department (ADMIN only)")
    public DepartmentResponse update(@PathVariable("id") Long id, @Valid @RequestBody DepartmentUpdateRequest req) {
        return departmentService.update(id, req);
    }
}
