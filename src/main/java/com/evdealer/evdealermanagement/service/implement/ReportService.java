package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.post.report.ReportRequest;
import com.evdealer.evdealermanagement.dto.post.report.ReportResponse;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.entity.report.Report;
import com.evdealer.evdealermanagement.repository.ProductRepository;
import com.evdealer.evdealermanagement.repository.ReportRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final ProductRepository productRepository;

    public ReportResponse createReport(ReportRequest request){
        Product product = productRepository.findById(request.getProductId()).orElseThrow(()->new EntityNotFoundException("product not found"));

        Report report = Report.builder()
                .product(product)
                .phone(request.getPhone())
                .email(request.getEmail())
                .reportReason(request.getRepostReason())
                .status(Report.ReportStatus.PENDING)
                .build();

        Report saved = reportRepository.save(report);

        return ReportResponse.builder().build();
              //  .id(saved.getId())
               // .productId()
    }

}
