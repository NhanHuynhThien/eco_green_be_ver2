package com.evdealer.evdealermanagement.controller.report;

import com.evdealer.evdealermanagement.dto.post.report.ProductReportCountResponse;
import com.evdealer.evdealermanagement.dto.post.report.ReportRequest;
import com.evdealer.evdealermanagement.dto.post.report.ReportResponse;
import com.evdealer.evdealermanagement.service.implement.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ReportResponse> create(@RequestBody ReportRequest request) {
        return ResponseEntity.ok(reportService.createReport(request));
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<Page<ProductReportCountResponse>> getReportStatistics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ProductReportCountResponse> reportStats = reportService.getProductReportStatistic(page, size);
        return ResponseEntity.ok(reportStats);
    }

    @GetMapping("/show")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<Page<ReportResponse>> showListReport(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ReportResponse> result = reportService.getReportPage(page, size);
        if (result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }
}
