package com.evdealer.evdealermanagement.dto.post.report;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {


    String productId;
    String phone;
    String email;
    String reportReason;
}
