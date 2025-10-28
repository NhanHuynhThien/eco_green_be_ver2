// File: ContractInfoDTO.java (đã sửa constructor để gán giá trị)
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

    // Sửa: Constructor tùy chỉnh giờ gán giá trị cho contractId và contractUrl
    // (Giả định map documentHash -> contractId, embeddedSigningUrl -> contractUrl)
    public ContractInfoDTO(String documentHash, String embeddedSigningUrl) {
        this.contractId = documentHash;
        this.contractUrl = embeddedSigningUrl;
        // Các trường khác (buyerSignUrl, sellerSignUrl, status) để null hoặc set mặc định nếu cần
    }
}