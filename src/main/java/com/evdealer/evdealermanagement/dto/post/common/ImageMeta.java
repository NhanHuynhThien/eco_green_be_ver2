package com.evdealer.evdealermanagement.dto.post.common;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class ImageMeta {

    Integer position;
    Boolean isPrimary;
}
