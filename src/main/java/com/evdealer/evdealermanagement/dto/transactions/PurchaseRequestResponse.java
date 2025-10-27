package com.evdealer.evdealermanagement.dto.transactions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestResponse {
    private String id;
    private String productId;
    private String productTitle;
    private String productThumbnail;
    private BigDecimal productPrice;

    private String buyerId;
    private String buyerName;
    private String buyerEmail;
    private String buyerPhone;

    private String sellerId;
    private String sellerName;
    private String sellerEmail;
    private String sellerPhone;

    private BigDecimal offeredPrice;
    private String buyerMessage;
    private String sellerResponseMessage;

    private String status;
    private String contractStatus;
    private String contractUrl;
    private String contractId;

    private String rejectReason;

    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
    private LocalDateTime buyerSignedAt;
    private LocalDateTime sellerSignedAt;
}