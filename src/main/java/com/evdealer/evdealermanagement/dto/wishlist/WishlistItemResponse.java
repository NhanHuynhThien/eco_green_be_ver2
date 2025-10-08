package com.evdealer.evdealermanagement.dto.wishlist;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
}