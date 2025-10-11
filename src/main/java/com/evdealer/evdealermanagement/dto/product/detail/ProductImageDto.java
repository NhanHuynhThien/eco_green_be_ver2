package com.evdealer.evdealermanagement.dto.product.detail;

import com.evdealer.evdealermanagement.entity.product.ProductImages;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageDto {
    private String id;
    private String imageUrl;
    private String publicId;
    private Boolean isPrimary;
    private Integer position;
    private Integer width;
    private Integer height;
    private Integer bytes;
    private String format;

    public static ProductImageDto fromEntity(ProductImages image) {
        if (image == null) return null;

        return ProductImageDto.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .publicId(image.getPublicId())
                .isPrimary(image.getIsPrimary())
                .position(image.getPosition())
                .width(image.getWidth())
                .height(image.getHeight())
                .bytes(image.getBytes())
                .format(image.getFormat())
                .build();
    }
}
