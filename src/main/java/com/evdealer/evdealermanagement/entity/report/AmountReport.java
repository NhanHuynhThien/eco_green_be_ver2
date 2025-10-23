package com.evdealer.evdealermanagement.entity.report;

import com.evdealer.evdealermanagement.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = false) // don't include BaseEntity fields
@Entity
@Table(name = "amount_report")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AmountReport {

    @Id
    @Column(name = "product_id", length = 36)
    String productId; // primary key

    @Column(nullable = false)
    Integer amount;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
