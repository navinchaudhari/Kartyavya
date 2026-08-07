package com.kartyavya.access.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "officer_department_assignments")
@Getter
@Setter
@NoArgsConstructor
public class OfficerAssignment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "officer_id", nullable = false, unique = true)
	private User officer;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "department_id", nullable = false)
	private Department department;
	
	@Column(nullable = false)
	private boolean active = true;
	
	@Column(name = "assigned_at", nullable = false)
	private Instant assignedAt;

	@PrePersist
	void create() {
		if (assignedAt == null)
			assignedAt = Instant.now();
	}
}
