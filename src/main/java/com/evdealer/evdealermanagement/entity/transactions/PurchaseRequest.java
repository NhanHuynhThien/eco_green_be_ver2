package com.evdealer.evdealermanagement.entity.transactions;

import com.evdealer.evdealermanagement.entity.BaseEntity;
import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.entity.product.Product;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PurchaseRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Account buyer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seller_id", nullable = false)
    private Account seller;

    @Column(name = "offered_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal offeredPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "buyer_message", columnDefinition = "TEXT")
    private String buyerMessage;

    @Column(name = "seller_response_message", columnDefinition = "TEXT")
    private String sellerResponseMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "contract_id", length = 100)
    private String contractId;

    @Column(name = "contract_url", length = 500)
    private String contractUrl;

    @Column(name = "buyer_signed_at")
    private LocalDateTime buyerSignedAt;

    @Column(name = "seller_signed_at")
    private LocalDateTime sellerSignedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_status", length = 20)
    private ContractStatus contractStatus;

    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    public enum RequestStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        CONTRACT_SENT,
        COMPLETED,
        CANCELLED,
        EXPIRED
    }

    public enum ContractStatus {
        PENDING,
        SENT,
        BUYER_SIGNED,
        SELLER_SIGNED,
        COMPLETED,
        CANCELLED
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
