package com.kartyavya.report.mapper;

import com.kartyavya.report.dto.ReportRequestDTO;
import com.kartyavya.report.dto.ReportResponseDTO;
import com.kartyavya.report.entity.Report;

public class ReportMapper {


    // DTO → Entity
    public static Report toEntity(ReportRequestDTO dto) {

        Report report = new Report();

        report.setReporterId(dto.getReporterId());
        report.setTitle(dto.getTitle());
        report.setDescription(dto.getDescription());
        report.setLatitude(dto.getLatitude());
        report.setLongitude(dto.getLongitude());
        report.setCategory(dto.getCategory());
        report.setSeverity(dto.getSeverity());

        return report;
    }



    // Entity → Response DTO
    public static ReportResponseDTO toResponseDTO(Report report) {

        ReportResponseDTO dto = new ReportResponseDTO();

        dto.setId(report.getId());
        dto.setTrackingCode(report.getTrackingCode());
        dto.setReporterId(report.getReporterId());
        dto.setTitle(report.getTitle());
        dto.setDescription(report.getDescription());
        dto.setLatitude(report.getLatitude());
        dto.setLongitude(report.getLongitude());
        dto.setCategory(report.getCategory());
        dto.setSeverity(report.getSeverity());
        dto.setStatus(report.getStatus());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setUpdatedAt(report.getUpdatedAt());

        return dto;
    }

}