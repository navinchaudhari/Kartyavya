package com.kartyavya.report.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kartyavya.report.service.ReportService;
import jakarta.validation.Valid;

import com.kartyavya.report.dto.ReportRequestDTO;
import com.kartyavya.report.dto.ReportResponseDTO;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }


    // CREATE
    @PostMapping
    public ResponseEntity<ReportResponseDTO> createReport(
            @Valid @RequestBody ReportRequestDTO request) {

        return ResponseEntity.ok(
                service.createReport(request)
        );
    }


    // GET ALL
    @GetMapping
    public ResponseEntity<List<ReportResponseDTO>> getReports() {

        return ResponseEntity.ok(
                service.getAllReports()
        );
    }


    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ReportResponseDTO> getReportById(
            @PathVariable("id") Long id) {

        return ResponseEntity.ok(
                service.getReportById(id)
        );
    }


    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ReportResponseDTO> updateReport(
            @PathVariable("id") Long id,
            @Valid @RequestBody ReportRequestDTO request) {

        return ResponseEntity.ok(
                service.updateReport(id, request)
        );
    }


    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReport(
            @PathVariable("id") Long id) {

        service.deleteReport(id);

        return ResponseEntity.ok(
                "Report deleted successfully"
        );
    }

}