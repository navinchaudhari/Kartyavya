package com.kartyavya.report.service;


import java.util.List;

import org.springframework.stereotype.Service;

import com.kartyavya.report.entity.Report;
import com.kartyavya.report.repository.ReportRepository;


@Service
public class ReportService {


    private final ReportRepository repository;


    public ReportService(ReportRepository repository){
        this.repository=repository;
    }


    public List<Report> getAllReports(){

        return repository.findAll();

    }


    public Report saveReport(Report report){

        return repository.save(report);

    }

}