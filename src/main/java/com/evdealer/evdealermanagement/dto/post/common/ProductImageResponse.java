package com.evdealer.evdealermanagement.dto.post.common;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageResponse {

    String id;
    String url;
    boolean isPrimary;
    Integer position;
    Integer width;
    Integer height;

}
