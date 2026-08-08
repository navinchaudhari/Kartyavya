package com.kartyavya.report.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports")
@Data
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(
            name = "tracking_code",
            nullable = false,
            unique = true,
            length = 36,
            columnDefinition = "CHAR(36)",
            updatable = false
    )
    private String trackingCode;


    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;


    @Column(nullable = false, length = 150)
    private String title;


    @Column(nullable = false, length = 2000)
    private String description;


    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;


    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;


    @Column(length = 40)
    private String category;


    @Column(length = 20)
    private String severity;


    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;


    @Column(name = "urgency_score", precision = 5, scale = 4)
    private BigDecimal urgencyScore;


    @Column(nullable = false, length = 30)
    private String status = "SUBMITTED";


    private Long assignedDepartmentId;


    @Version
    private Long version;


    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;



    // Runs automatically before saving new report
    @PrePersist
    public void prePersist(){

        if(trackingCode == null || trackingCode.isEmpty()){
            trackingCode = UUID.randomUUID().toString();
        }

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }



    // Runs automatically before updating report
    @PreUpdate
    public void preUpdate(){

        updatedAt = LocalDateTime.now();

    }

}