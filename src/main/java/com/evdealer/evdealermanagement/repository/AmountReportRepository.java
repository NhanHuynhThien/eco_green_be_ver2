package com.evdealer.evdealermanagement.repository;

import com.evdealer.evdealermanagement.entity.report.AmountReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmountReportRepository extends JpaRepository<AmountReport, String> {
    List<AmountReport> findByProductIdIn(List<String> productIds);
}
