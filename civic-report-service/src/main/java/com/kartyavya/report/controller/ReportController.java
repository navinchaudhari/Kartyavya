package com.kartyavya.report.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kartyavya.report.entity.Report;
import com.kartyavya.report.service.ReportService;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }


    @PostMapping
    public ResponseEntity<Report> createReport(
            @RequestBody Report report) {

        return ResponseEntity.ok(service.saveReport(report));
    }


    @GetMapping
    public ResponseEntity<List<Report>> getReports() {

        return ResponseEntity.ok(service.getAllReports());
    }


    @GetMapping("/{id}")
    public ResponseEntity<Report> getReportById(
            @PathVariable("id") Long id) {

        Report report = service.getReportById(id);

        return ResponseEntity.ok(report);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Report> updateReport(
            @PathVariable("id") Long id,
            @RequestBody Report report) {

        Report updated = service.updateReport(id, report);

        return ResponseEntity.ok(updated);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReport(
            @PathVariable("id") Long id) {

        service.deleteReport(id);

        return ResponseEntity.ok("Report deleted successfully");
    }
}