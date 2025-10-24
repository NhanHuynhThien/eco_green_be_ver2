package com.evdealer.evdealermanagement.dto.post.report;

import com.evdealer.evdealermanagement.entity.report.Report;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportResponse {

    String id;
    String productId;
    String productName;
    String phone;
    String email;
    String reportReason;
    Report.ReportStatus status;
    LocalDateTime createdAt;
    LocalDateTime updateAt;
}
