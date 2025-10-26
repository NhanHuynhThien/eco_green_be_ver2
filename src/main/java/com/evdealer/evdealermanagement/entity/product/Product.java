package com.evdealer.evdealermanagement.entity.product;

import com.evdealer.evdealermanagement.entity.BaseEntity;
import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.entity.battery.BatteryDetails;
import com.evdealer.evdealermanagement.entity.vehicle.ModelVersion;

import com.evdealer.evdealermanagement.entity.vehicle.VehicleDetails;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class Product extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ENUM('BATTERY','VEHICLE')
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ProductType type;

    @Column(precision = 15, scale = 2)
    private BigDecimal price;

    // ENUM('NEW','USED')
    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false, length = 10)
    private ConditionType conditionType;

    // ENUM('ACTIVE','DRAFT','SOLD') theo DB hiện tại
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    // seller_id CHAR(36)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seller_id", nullable = false)
    private Account seller;

    // created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    // updated_at DATETIME(6)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Địa chỉ hiển thị
    @Column(name = "address_detail", columnDefinition = "TEXT")
    private String addressDetail;

    @Column(name = "city", length = 255)
    private String city;

    @Column(name = "district", length = 255)
    private String district;

    @Column(name = "ward", length = 255)
    private String ward;

    @Column(name = "seller_phone", length = 255)
    private String sellerPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "sale_type", length = 20)
    private SaleType saleType;

    @Column(name = "auction_end_time")
    private LocalDateTime auctionEndTime;

    @Column(name = "posting_fee", precision = 8, scale = 2)
    private BigDecimal postingFee;

    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approved_by")
    private Account approvedBy;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductImages> images;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_version_id")
    private ModelVersion modelVersion;

    @Column(name = "manufacture_year")
    private Short manufactureYear;

    @Column(name = "featured_end_at")
    private LocalDateTime featuredEndAt;
    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private VehicleDetails vehicleDetails;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private BatteryDetails batteryDetails;

    @Column(name = "is_hot")
    private Boolean isHot = false;

    public enum ProductType {
        BATTERY, VEHICLE
    }

    public enum ConditionType {
        NEW, // ← THÊM DÒNG NÀY
        USED
    }

    public enum Status {
        DRAFT, ACTIVE, SOLD, PENDING_REVIEW, PENDING_PAYMENT, REJECTED, EXPIRED, HIDDEN
    }

    public enum SaleType {
        AUCTION, FIXED_PRICE, NEGOTIATION
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

}