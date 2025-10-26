package com.evdealer.evdealermanagement.mapper.post;

import org.hibernate.Hibernate;

import com.evdealer.evdealermanagement.dto.post.verification.PostVerifyResponse;
import com.evdealer.evdealermanagement.entity.battery.BatteryDetails;
import com.evdealer.evdealermanagement.entity.post.PostPayment;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.entity.vehicle.VehicleDetails;

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

        String brandName = null;
        String modelName = null;
        String version = null;
        String batteryType = null;

        if (p.getType() == Product.ProductType.VEHICLE) {
            Hibernate.initialize(p.getVehicleDetails());
            VehicleDetails vehicleDetails = p.getVehicleDetails();

            if (vehicleDetails != null && vehicleDetails.getVersion() != null) {
                version = vehicleDetails.getVersion().getName();

                if (vehicleDetails.getBrand() != null && vehicleDetails.getBrand().getName() != null) {
                    brandName = vehicleDetails.getBrand().getName();
                }
                if (vehicleDetails.getModel() != null && vehicleDetails.getModel().getName() != null) {
                    modelName = vehicleDetails.getModel().getName();
                }
            }
        } else if (p.getType() == Product.ProductType.BATTERY) {
            Hibernate.initialize(p.getBatteryDetails());
            BatteryDetails batteryDetails = p.getBatteryDetails();

            if (batteryDetails != null && batteryDetails.getBrand() != null) {
                brandName = batteryDetails.getBrand().getName();

                if (batteryDetails.getBatteryType() != null) {
                    batteryType = batteryDetails.getBatteryType().getName();
                }
            }
        }

        return PostVerifyResponse.builder()
                .id(p.getId())
                .status(p.getStatus())
                .title(p.getTitle())
                .thumbnail(thumbnail)
                .productType(p.getType())
                .sellerName(p.getSeller().getFullName())
                .sellerId(p.getSeller().getId())
                .sellerPhone(p.getSellerPhone())
                .updateAt(p.getUpdatedAt())
                .brandName(brandName)
                .batteryType(batteryType)
                .modelName(modelName)
                .versionName(version)
                .featuredEndAt(p.getFeaturedEndAt())
                .expiresAt(p.getExpiresAt())
                .packageName(payment != null && payment.getPostPackage() != null
                        ? payment.getPostPackage().getName()
                        : null)
                .amount(payment != null ? payment.getAmount() : null)
                .build();
    }

}
