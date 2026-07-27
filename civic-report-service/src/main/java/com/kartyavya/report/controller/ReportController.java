package com.kartyavya.report.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.kartyavya.report.entity.Report;
import com.kartyavya.report.service.ReportService;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service){
        this.service = service;
    }


    @GetMapping("/{id}")
    public Report getReportById(@PathVariable("id") Long id){

        return service.getReportById(id);
    }


    @GetMapping
    public List<Report> getReports(){

        return service.getAllReports();
    }


    @PostMapping
    public Report createReport(
            @RequestBody Report report){

        return service.saveReport(report);
    }

}