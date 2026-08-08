package com.kartyavya.report.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

import java.math.BigDecimal;


@Data
public class ReportRequestDTO {


    @NotNull(message = "Reporter id is required")
    private Long reporterId;


    @NotBlank(message = "Title cannot be empty")
    @Size(max = 150, message = "Title must be less than 150 characters")
    private String title;


    @NotBlank(message = "Description cannot be empty")
    @Size(max = 2000, message = "Description must be less than 2000 characters")
    private String description;


    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Invalid latitude")
    @DecimalMax(value = "90.0", message = "Invalid latitude")
    private BigDecimal latitude;


    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Invalid longitude")
    @DecimalMax(value = "180.0", message = "Invalid longitude")
    private BigDecimal longitude;


    @Size(max = 40, message = "Category must be less than 40 characters")
    private String category;


    @Size(max = 20, message = "Severity must be less than 20 characters")
    private String severity;
}