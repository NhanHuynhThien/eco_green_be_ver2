package com.evdealer.evdealermanagement.mapper.report;

import com.evdealer.evdealermanagement.dto.post.report.ReportResponse;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.entity.report.Report;

public final class ReportMapper {
    private ReportMapper() {
    }

    public static ReportResponse toResponse(Report r) {
        if (r == null)
            return null;
        Product p = r.getProduct();
        return ReportResponse.builder()
                .id(r.getId())
                .productId(p != null ? p.getId() : null)
                .productName(p != null ? p.getTitle() : null)
                .phone(r.getPhone())
                .email(r.getEmail())
                .reportReason(r.getReportReason())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .updateAt(r.getUpdatedAt())
                .build();
    }
}