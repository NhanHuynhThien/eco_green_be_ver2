package com.evdealer.evdealermanagement.dto.wishlist;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WishlistItemResponse {
    String productName;
    String thumbnailUrl;
    String productId;
    LocalDateTime addedAt;
    Boolean isWishlisted;
    BigDecimal price;
    String addressDetail;
    String city;
    String district;
    String ward;
}