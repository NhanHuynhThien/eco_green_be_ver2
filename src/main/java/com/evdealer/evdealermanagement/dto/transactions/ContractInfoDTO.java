package com.evdealer.evdealermanagement.dto.transactions;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractInfoDTO {
    private String contractId;
    private String contractUrl;
    private String buyerSignUrl;
    private String sellerSignUrl;
    private String status;
}