package com.evdealer.evdealermanagement.repository;

import com.evdealer.evdealermanagement.entity.report.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, String> {

    /**
     * Thống kê số lượng report theo product, sắp xếp giảm dần
     * Return: [productId, productTitle, reportCount]
     */
    @Query("SELECT r.product.id, r.product.title, COUNT(r) as reportCount " +
            "FROM Report r " +
            "GROUP BY r.product.id, r.product.title " +
            "ORDER BY reportCount DESC")
    Page<Object[]> findProductReportStatistics(Pageable pageable);

    @EntityGraph(attributePaths = { "product" })
    Page<Report> findAllBy(Pageable pageable);
}