package com.kartyavya.access.entity;

import jakarta.persistence.*;

// Owner: M1. Maps to user_roles table — shared-PK pattern: user_id is BOTH PK and FK to users.
// Schema rule: one role per user in v1 enforced by PRIMARY KEY (user_id).
@Entity
@Table(name = "user_roles")
public class UserRole {

    @Id
    @Column(name = "user_id")
    private Long id;

    // @MapsId copies user.id into this entity's id field — no separate @GeneratedValue.
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
