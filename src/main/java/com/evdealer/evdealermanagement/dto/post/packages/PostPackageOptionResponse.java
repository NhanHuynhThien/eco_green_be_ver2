package com.evdealer.evdealermanagement.dto.post.packages;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostPackageOptionResponse {

    String id;
    String name;
    Integer durationDays;
    BigDecimal price;
    BigDecimal listPrice;
    Boolean isDefault;
    Integer sortOrder;
}
