package com.kartyavya.access.entity;

import jakarta.persistence.*;
import java.time.Instant;

// Owner: M1. Maps to officer_department_assignments table — see V1__init_access_schema.sql.
// officer_id is stored as a plain Long (FK to users.id) to allow findByOfficerId derived query.
@Entity
@Table(name = "officer_department_assignments")
public class OfficerDepartmentAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK to users.id enforced by DB constraint; stored as Long for simple repository query.
    @Column(name = "officer_id", nullable = false, unique = true)
    private Long officerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    @Column(nullable = false)
    private boolean active = true;

    @PrePersist
    protected void onCreate() {
        if (assignedAt == null) {
            assignedAt = Instant.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOfficerId() { return officerId; }
    public void setOfficerId(Long officerId) { this.officerId = officerId; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public Instant getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Instant assignedAt) { this.assignedAt = assignedAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
