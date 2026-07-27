package com.kartyavya.report.service;

import com.kartyavya.report.exception.ReportNotFoundException;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kartyavya.report.entity.Report;
import com.kartyavya.report.repository.ReportRepository;


@Service
public class ReportService {


    private final ReportRepository repository;


    public ReportService(ReportRepository repository){

        this.repository = repository;
    }



    // CREATE
    public Report saveReport(Report report){

        return repository.save(report);
    }



    // GET ALL
    public List<Report> getAllReports(){

        return repository.findAll();
    }



    // GET BY ID
    public Report getReportById(Long id){

        return repository.findById(id)
                .orElseThrow(
                    () -> new RuntimeException(
                        "Report not found : " + id
                    )
                );
    }



    // UPDATE
    public Report updateReport(Long id, Report newReport){


        Report existing = repository.findById(id)
                .orElseThrow(
                    () -> new RuntimeException(
                        "Report not found : " + id
                    )
                );


        existing.setTitle(newReport.getTitle());

        existing.setDescription(
                newReport.getDescription()
        );

        existing.setCategory(
                newReport.getCategory()
        );

        existing.setSeverity(
                newReport.getSeverity()
        );

        existing.setStatus(
                newReport.getStatus()
        );

        existing.setLatitude(
                newReport.getLatitude()
        );

        existing.setLongitude(
                newReport.getLongitude()
        );


        return repository.save(existing);

    }



    // DELETE
    public void deleteReport(Long id){

        Report report = repository.findById(id)
                .orElseThrow(
                    () -> new ReportNotFoundException(
                        "Report not found : " + id
                    )
                );

        repository.delete(report);
    }

}