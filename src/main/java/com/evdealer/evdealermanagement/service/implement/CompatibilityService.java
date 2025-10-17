package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.compatibility.CompatibilityRequest;
import com.evdealer.evdealermanagement.dto.compatibility.CompatibilityResponse;
import com.evdealer.evdealermanagement.dto.compatibility.ProductDetailResponse;
import com.evdealer.evdealermanagement.entity.compatibility.VehicleBatteryCompatibility;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.exceptions.AppException;
import com.evdealer.evdealermanagement.exceptions.ErrorCode;
import com.evdealer.evdealermanagement.repository.ProductRepository;
import com.evdealer.evdealermanagement.repository.VehicleBatteryCompatibilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompatibilityService {

    private final ProductRepository productRepository;
    private final VehicleBatteryCompatibilityRepository vehicleBatteryCompatibilityRepository;

    public ProductDetailResponse getProductDetail(CompatibilityRequest request) {

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        List<VehicleBatteryCompatibility> compatibilities;
        if("VEHICLE".equalsIgnoreCase(product.getType().name())) {
            compatibilities = vehicleBatteryCompatibilityRepository.findByVehicle(product);
        } else if("BATTERY".equalsIgnoreCase(product.getType().name())) {
            compatibilities = vehicleBatteryCompatibilityRepository.findByBattery(product);
        } else {
            compatibilities = List.of();
        }

        List<CompatibilityResponse> compatibilityResponses = compatibilities.stream()
                .map(c -> {
                    Product p = "VEHICLE".equalsIgnoreCase(product.getType().name())
                            ? c.getBattery()
                            : c.getVehicle();

                    return CompatibilityResponse.builder()
                            .productId(p.getId())
                            .productName(p.getTitle())
                            .compatibilityLevel(c.getCompatibilityLevel())
                            .build();
                })
                .collect(Collectors.toList());

        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getTitle())
                .category(product.getType().name())
                .price(product.getPrice())
                .compatibilityList(compatibilityResponses)
                .build();
    }
}
