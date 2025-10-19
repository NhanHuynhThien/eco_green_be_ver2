package com.evdealer.evdealermanagement.mapper.post;

import com.evdealer.evdealermanagement.dto.post.verification.PostVerifyResponse;
import com.evdealer.evdealermanagement.entity.post.PostPayment;
import com.evdealer.evdealermanagement.entity.product.Product;

public class PostVerifyMapper {

    private PostVerifyMapper() {
    }

    public static PostVerifyResponse mapToPostVerifyResponse(Product p, PostPayment payment) {
        if (p == null)
            return null;

        // Thumbnail: lấy ảnh đầu tiên nếu có
        String thumbnail = null;
        if (p.getImages() != null && !p.getImages().isEmpty() && p.getImages().get(0) != null) {
            // Đổi getUrl() theo field thực tế của ProductImages (vd: getImageUrl())
            thumbnail = p.getImages().get(0).getImageUrl();
        }

        String versionName = null;
        String modelName = null;
        if (p.getModelVersion() != null) {
            versionName = p.getModelVersion().getName();
            if (p.getModelVersion().getModel() != null) {
                modelName = p.getModelVersion().getModel().getName();
            }
        }

        return PostVerifyResponse.builder()
                .id(p.getId())
                .status(p.getStatus())
                .title(p.getTitle())
                .thumbnail(thumbnail)
                .productType(p.getType())
                .updateAt(p.getUpdatedAt())
                .modelName(modelName)
                .versionName(versionName)
                .packageName(payment != null && payment.getPostPackage() != null
                        ? payment.getPostPackage().getName()
                        : null)
                .amount(payment != null ? payment.getAmount() : null)
                .build();
    }

    public static PostVerifyResponse mapToPostVerifyResponse(Product product) {

        if (product == null) {
            return null;
        }

        return PostVerifyResponse.builder()
                .id(product.getId() != null ? product.getId().toString() : null)
                .status(product.getStatus())
                .rejectReason(product.getRejectReason())
                .updateAt(product.getUpdatedAt())
                .build();
    }
}
