package com.evdealer.evdealermanagement.entity.post;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "post_package_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostPackageOption {

    @Id
    @Column(length = 36)
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    // Liên kết về gói cha
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    PostPackage postPackage;

    @Column(length = 100, nullable = false)
    String name; // Ví dụ: "1 Ngày", "3 Ngày", "5 Ngày"

    @Column(name = "duration_days")
    Integer durationDays; // Số ngày của lựa chọn

    @Column(precision = 10, scale = 2, nullable = false)
    BigDecimal price; // Giá bán thực tế

    @Column(name = "list_price", precision = 10, scale = 2)
    BigDecimal listPrice; // Giá gốc (nếu có khuyến mãi)

    @Column(name = "is_default", nullable = false)
    Boolean isDefault; // 1 = lựa chọn mặc định

    @Column(name = "sort_order", nullable = false)
    Integer sortOrder; // Thứ tự hiển thị option

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('ACTIVE','INACTIVE') default 'ACTIVE'")
    Status status;

    @Column(name = "created_at", updatable = false, insertable = false)
    Instant createdAt;

    @Column(name = "updated_at", insertable = false)
    Instant updatedAt;

    // --- ENUM ---
    public enum Status {
        ACTIVE, INACTIVE
    }

}
