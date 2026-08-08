package com.kartyavya.report.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.kartyavya.report.entity.Report;


public interface ReportRepository 
        extends JpaRepository<Report,Long>{

}