package com.evdealer.evdealermanagement.repository;

import com.evdealer.evdealermanagement.entity.report.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report,String> {
}
