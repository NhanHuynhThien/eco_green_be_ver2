package com.evdealer.evdealermanagement.entity.post;

import com.evdealer.evdealermanagement.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "post_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostPayment extends BaseEntity {

    @Column(name = "account_id",nullable = false, length = 36)
    private String accountId;

    @Column(name = "product_id", nullable = false, length = 36)
    private String productId;

    @Column(name = "package_id", nullable = false, length = 36)
    private String packageId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    public enum PaymentMethod {
        CASH, BANK_TRANSFER, MOMO, ZALO_PAY, VNPAY
    }

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED
    }
}
