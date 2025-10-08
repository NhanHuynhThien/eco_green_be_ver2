package com.evdealer.evdealermanagement.entity.product;

import com.evdealer.evdealermanagement.entity.BaseEntity;
import com.evdealer.evdealermanagement.entity.account.Account;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// @Entity
// @Table(name = "products")
// @Getter
// @Setter
// @NoArgsConstructor
// @AllArgsConstructor
// @SuperBuilder
// public class Product extends BaseEntity {

//     @Column(nullable = false, length = 255)
//     private String title;

//     @Column(columnDefinition = "TEXT")
//     private String description;

//     @Enumerated(EnumType.STRING)
//     @Column(nullable = false, length = 20)
//     private ProductType type;

//     @Column(precision = 15, scale = 2)
//     private BigDecimal price;

//     @Enumerated(EnumType.STRING)
//     @Column(name = "condition_type", length = 10, nullable = false)
//     private ConditionType conditionType;

//     @Enumerated(EnumType.STRING)
//     @Column(length = 20, nullable = false)
//     private Status status;

//     @ManyToOne(fetch = FetchType.EAGER)
//     @JoinColumn(name = "seller_id", nullable = false)
//     private Account seller;

//     @Column(name = "created_at", updatable = false)
//     private LocalDateTime createdAt;

//     @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
//     private List<ProductImages> images;

//     public enum ProductType {
//         VEHICLE, BATTERY
//     }

//     public enum ConditionType {
//         NEW, USED
//     }

//     public enum Status {
//         DRAFT, ACTIVE, SOLD, PENDING_REVIEW, PENDING_PAYMENT, REJECTED, EXPIRED, HIDDEN
//     }
// }

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

    // Số điện thoại người bán
    @Column(name = "seller_phone", length = 255)
    private String sellerPhone;

    // Kiểu bán: ENUM('AUCTION','FIXED_PRICE')
    @Enumerated(EnumType.STRING)
    @Column(name = "sale_type", length = 20)
    private SaleType saleType;

    // Thời điểm kết thúc đấu giá (nếu sale_type = AUCTION)
    @Column(name = "auction_end_time")
    private LocalDateTime auctionEndTime;

    // Có thương lượng giá hay không: BIT(1)
    @Column(name = "is_negotiable", columnDefinition = "BIT")
    private Boolean isNegotiable;

    // Phí đăng tin DECIMAL(8,2)
    @Column(name = "posting_fee", precision = 8, scale = 2)
    private BigDecimal postingFee;

    // Lý do bị từ chối (nếu có)
    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    // Thời điểm hết hạn tin
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // Người duyệt (approved_by VARCHAR(36)) – giả định tham chiếu Account
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approved_by")
    private Account approvedBy;

    // Quan hệ ảnh sản phẩm
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImages> images;

    // ====== Enums khớp DB ======
    public enum ProductType {
        BATTERY, VEHICLE
    }

    public enum ConditionType {
        NEW, USED
    }

    public enum Status {
        DRAFT, ACTIVE, SOLD, PENDING_REVIEW, PENDING_PAYMENT, REJECTED, EXPIRED, HIDDEN
    }
}