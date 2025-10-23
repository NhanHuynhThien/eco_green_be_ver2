package com.evdealer.evdealermanagement.controller.report;

import com.evdealer.evdealermanagement.dto.post.report.ReportRequest;
import com.evdealer.evdealermanagement.dto.post.report.ReportResponse;
import com.evdealer.evdealermanagement.service.implement.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ReportResponse> create(@RequestBody ReportRequest request){
       return ResponseEntity.ok(reportService.createReport(request));
    }
}
