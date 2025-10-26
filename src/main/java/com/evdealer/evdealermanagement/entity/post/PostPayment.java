package com.evdealer.evdealermanagement.entity.post;

import com.evdealer.evdealermanagement.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "post_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostPayment extends BaseEntity {

    @Column(name = "account_id", nullable = false, length = 36)
    private String accountId;

    @Column(name = "product_id", nullable = false, length = 36)
    private String productId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_option_id", foreignKey = @ForeignKey(name = "fk_pp_opt"))
    PostPackageOption postPackageOption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false, foreignKey = @ForeignKey(name = "fk_payment_package"))
    private PostPackage postPackage;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum PaymentMethod {
        CASH, BANK_TRANSFER, MOMO, ZALO_PAY, VNPAY
    }

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED
    }
}
