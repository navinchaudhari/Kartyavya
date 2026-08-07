package com.kartyavya.access.controller;

import com.kartyavya.access.dto.AdminDtos.*;
import com.kartyavya.access.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
public class AdminController {
	private final AdminService s;

	public AdminController(AdminService s) {
		this.s = s;
	}

	@GetMapping("/api/departments")
	List<DepartmentResponse> publicDepartments() {
		return s.departments(true);
	}

	@GetMapping("/api/admin/departments")
	List<DepartmentResponse> departments() {
		return s.departments(false);
	}

	@PostMapping("/api/admin/departments")
	ResponseEntity<DepartmentResponse> create(@Valid @RequestBody DepartmentRequest r) {
		return ResponseEntity.status(201).body(s.createDepartment(r));
	}

	@PutMapping("/api/admin/departments/{id}")
	DepartmentResponse update(@PathVariable Long id, @Valid @RequestBody DepartmentRequest r) {
		return s.updateDepartment(id, r);
	}

	@DeleteMapping("/api/admin/departments/{id}")
	Object disable(@PathVariable Long id) {
		s.disableDepartment(id);
		return Map.of("message", "Department disabled");
	}

	@GetMapping("/api/admin/officers")
	List<OfficerResponse> officers() {
		return s.officers();
	}

	@PostMapping("/api/admin/officers")
	ResponseEntity<OfficerResponse> createOfficer(@Valid @RequestBody OfficerCreate r) {
		return ResponseEntity.status(201).body(s.createOfficer(r));
	}

	@PutMapping("/api/admin/officers/{id}")
	OfficerResponse updateOfficer(@PathVariable Long id, @Valid @RequestBody OfficerUpdate r) {
		return s.updateOfficer(id, r);
	}

	@DeleteMapping("/api/admin/officers/{id}")
	Object disableOfficer(@PathVariable Long id) {
		s.disableOfficer(id);
		return Map.of("message", "Officer disabled");
	}

	@GetMapping("/api/admin/routing-rules")
	List<RoutingRuleResponse> rules() {
		return s.rules();
	}

	@PostMapping("/api/admin/routing-rules")
	RoutingRuleResponse createRule(@Valid @RequestBody RoutingRuleRequest r) {
		return s.saveRule(null, r);
	}

	@PutMapping("/api/admin/routing-rules/{id}")
	RoutingRuleResponse updateRule(@PathVariable Long id, @Valid @RequestBody RoutingRuleRequest r) {
		return s.saveRule(id, r);
	}

	@GetMapping("/api/admin/users/citizens")
	List<UserResponse> citizens() {
		return s.citizens();
	}
}
