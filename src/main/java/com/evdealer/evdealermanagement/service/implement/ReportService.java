package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.post.report.ReportRequest;
import com.evdealer.evdealermanagement.dto.post.report.ReportResponse;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.entity.report.Report;
import com.evdealer.evdealermanagement.repository.ProductRepository;
import com.evdealer.evdealermanagement.repository.ReportRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    private final ReportRepository reportRepository;
    private final ProductRepository productRepository;

    public ReportResponse createReport(ReportRequest request){
        log.info("[REPORT] Received new report request for product {}", request.getProductId());
        log.debug("[REPORT] Request detail: {}", request);
        Product product = productRepository.findById(request.getProductId()).orElseThrow(()->new EntityNotFoundException("product not found"));

        if (request.getProductId() == null || request.getProductId().isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getReportReason() == null || request.getReportReason().isBlank()) {
            throw new IllegalArgumentException("Report reason is required");
        }
        if (request.getReportReason().length() > 255) {
            throw new IllegalArgumentException("Report reason is too long (max 255 chars)");
        }
        
        Report report = Report.builder()
                .product(product)
                .phone(request.getPhone())
                .email(request.getEmail())
                .reportReason(request.getReportReason())
                .status(Report.ReportStatus.PENDING)
                .build();

        Report saved = reportRepository.save(report);
        log.info("[REPORT] Report {} saved successfully for product {}", saved.getId(), product.getId());

        return ReportResponse.builder()
                .id(saved.getId())
                .productId(product.getId())
                .phone(saved.getPhone())
                .email(saved.getEmail())
                .repostReason(saved.getReportReason())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .build();

    }

}
