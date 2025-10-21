package com.evdealer.evdealermanagement.dto.vehicle.detail;

import com.evdealer.evdealermanagement.dto.vehicle.catalog.VehicleCatalogResponse;
import com.evdealer.evdealermanagement.entity.vehicle.VehicleCatalog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDetailResponse {

    // Product info
    //private String productId;
    private String productTitle;
    private BigDecimal productPrice;
    private String productStatus;

    // Brand & Model info
    //private String brandId;
    private String brandName;
    private String brandLogoUrl;

    //private String modelId;
    private String modelName;

    //private String versionId;
    private String versionName;

    // Category info
    //private String categoryId;
    private String categoryName;

    // Vehicle Details (từ VehicleDetails entity)
    private Integer mileageKm;
    private Short batteryHealthPercent;
    private Boolean hasRegistration;
    private Boolean hasInsurance;
    private Byte warrantyMonths;

    private VehicleCatalogResponse vehicleCatalog;


}