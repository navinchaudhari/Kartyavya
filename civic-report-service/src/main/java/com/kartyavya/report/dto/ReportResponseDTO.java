package com.kartyavya.report.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
public class ReportResponseDTO {


    private Long id;

    private String trackingCode;

    private Long reporterId;

    private String title;

    private String description;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String category;

    private String severity;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}