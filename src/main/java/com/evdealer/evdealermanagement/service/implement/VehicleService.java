package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.vehicle.brand.VehicleBrandsRequest;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.evdealer.evdealermanagement.dto.battery.brand.BatteryBrandsResponse;
import com.evdealer.evdealermanagement.dto.vehicle.brand.VehicleBrandsResponse;
import com.evdealer.evdealermanagement.dto.vehicle.brand.VehicleCategoriesResponse;
import com.evdealer.evdealermanagement.dto.vehicle.catalog.VehicleCatalogResponse;
import com.evdealer.evdealermanagement.dto.vehicle.detail.VehicleDetailResponse;
import com.evdealer.evdealermanagement.dto.vehicle.model.VehicleModelRequest;
import com.evdealer.evdealermanagement.dto.vehicle.model.VehicleModelResponse;
import com.evdealer.evdealermanagement.dto.vehicle.model.VehicleModelVersionRequest;
import com.evdealer.evdealermanagement.dto.vehicle.model.VehicleModelVersionResponse;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.entity.vehicle.*;
import com.evdealer.evdealermanagement.exceptions.AppException;
import com.evdealer.evdealermanagement.exceptions.ErrorCode;
import com.evdealer.evdealermanagement.mapper.vehicle.VehicleMapper;
import com.evdealer.evdealermanagement.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleDetailsRepository vehicleDetailsRepository;
    private final VehicleCategoryRepository vehicleCategoryRepository;
    private final VehicleBrandsRepository vehicleBrandsRepository;
    private final Cloudinary cloudinary;
    private final VehicleModelRepository vmRepository;
    private final VehicleModelVersionRepository vmvRepository;
    private final ProductRepository productRepository;

    /**
     * Lấy danh sách Vehicle Product IDs theo tên hãng
     */
    public List<Long> getVehicleIdByBrand(String brand) {
        if (brand == null || brand.trim().isEmpty()) {
            log.warn("Vehicle brand is null or empty");
            return List.of();
        }

        try {
            log.debug("Getting vehicle IDs by brand: {}", brand);
            return vehicleDetailsRepository.findVehicleProductIdsByBrand(brand);
        } catch (Exception e) {
            log.error("Error getting vehicle IDs by brand: {}", brand, e);
            return List.of();
        }
    }

    public List<VehicleCategoriesResponse> listAllVehicleCategoriesSorted() {
        var all = vehicleCategoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        return all.stream().map(v -> new VehicleCategoriesResponse(v.getId(), v.getName()))
                .collect(Collectors.toList());
    }

    public List<VehicleBrandsResponse> listAllVehicleBrandsSorted() {
        var all = vehicleBrandsRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        return all.stream().map(v -> VehicleBrandsResponse.builder()
                .brandName(v.getName())
                .brandId(v.getId())
                .build())
                .collect(Collectors.toList());
    }

    public List<VehicleBrandsResponse> listAllVehicleNameAndLogo() {
        var all = vehicleBrandsRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        return all.stream().map(v -> VehicleBrandsResponse.builder()
                .brandName(v.getName())
                .logoUrl(v.getLogoUrl())
                .build())
                .collect(Collectors.toList());
    }

    public VehicleBrandsResponse addNewVehicleBrand(VehicleBrandsRequest req) {

        String brandName = req.getBrandName().trim();
        if (vehicleBrandsRepository.existsByNameIgnoreCase(brandName)) {
            throw new AppException(ErrorCode.BRAND_EXISTS, "Brand name already exists");
        }
        VehicleBrands e = new VehicleBrands();
        e.setName(brandName);
        e.setLogoUrl(req.getLogoUrl());
        e = vehicleBrandsRepository.save(e);
        return VehicleMapper.mapToVehicleBrandsResponse(e);
    }

    @Transactional
    public VehicleBrandsResponse createWithLogo(String brandName, MultipartFile logoFile) {
        // 1) Validate brandName
        if (brandName == null || brandName.isBlank()) {
            throw new AppException(ErrorCode.BRAND_NOT_FOUND);
        }
        String brandNameWithoutSpace = brandName.trim();

        // 2) Check trùng tên (ignore case)
        if (vehicleBrandsRepository.existsByNameIgnoreCase(brandNameWithoutSpace)) {
            throw new AppException(ErrorCode.BRAND_EXISTS);
        }

        // 3) Validate ảnh
        validateLogo(logoFile);

        // 4) Upload Cloudinary
        Map<String, Object> uploaded;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> up = (Map<String, Object>) cloudinary.uploader().upload(
                    logoFile.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "eco-green/brands/vehicle", // thư mục riêng cho brand vehicle
                            "resource_type", "image",
                            "overwrite", true,
                            "unique_filename", true));
            uploaded = up;
        } catch (Exception e) {
            log.error("Cloudinary upload error: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }

        String secureUrl = (String) uploaded.get("secure_url");

        // 5) Lưu DB
        VehicleBrands entity = new VehicleBrands();
        entity.setName(brandNameWithoutSpace);
        entity.setLogoUrl(secureUrl);
        vehicleBrandsRepository.save(entity);

        // 6) Trả DTO
        return VehicleBrandsResponse.builder()
                .brandId(entity.getId())
                .brandName(entity.getName())
                .logoUrl(entity.getLogoUrl())
                .build();
    }

    private void validateLogo(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new AppException(ErrorCode.MIN_1_IMAGE);
        }
        if (image.getSize() > 6L * 1024 * 1024) {
            throw new AppException(ErrorCode.IMAGE_TOO_LARGE);
        }
        String ct = image.getContentType() == null ? "" : image.getContentType();
        if (!(ct.equals("image/jpeg") || ct.equals("image/png"))) {
            throw new AppException(ErrorCode.UNSUPPORTED_IMAGE_TYPE);
        }
    }

    public List<VehicleModelResponse> listAllVehicleModelsSorted(VehicleModelRequest request) {
        var all = vmRepository.findAllByBrand_IdAndVehicleType_Id( request.getBrandId(), request.getCategoryId());
        return all.stream().map(m -> VehicleModelResponse.builder()
                .modelId(m.getId())
                .modelName(m.getName())
                .build())
                .collect(Collectors.toList());
    }

    public List<VehicleModelVersionResponse> listAllVehicleModelVersionsSorted(VehicleModelVersionRequest request) {
        var all = vmvRepository.findAllByModel_Id(request.getModelId());
        return all.stream().map(vmv -> VehicleModelVersionResponse.builder()
                .modelVersionId(vmv.getId())
                .modelVersionName(vmv.getName())
                .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public VehicleDetailResponse getVehicleDetailsInfo(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (product.getType() == null || !product.getType().name().equals("VEHICLE")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product is not a vehicle type");
        }

        VehicleDetails details = vehicleDetailsRepository.findByProductId(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle details not found"));

        Model model = details.getModel();
        VehicleBrands brand = details.getBrand();
        ModelVersion version = details.getVersion();
        VehicleCategories category = details.getCategory();
        VehicleCatalog catalog = details.getVehicleCatalog();

        VehicleCatalogResponse catalogResponse = null;
        if (catalog != null) {
            catalogResponse = VehicleCatalogResponse.builder()
                    .id(catalog.getId())
                    .year(Integer.valueOf(catalog.getYear()))
                    .type(catalog.getType())
                    .color(catalog.getColor())
                    .rangeKm(Double.valueOf(catalog.getRangeKm()))
                    .batteryCapacityKwh(catalog.getBatteryCapacityKwh())
                    .powerHp(catalog.getPowerHp())
                    .topSpeedKmh(catalog.getTopSpeedKmh())
                    .acceleration0100s(catalog.getAcceleration0100s())
                    .weightKg(catalog.getWeightKg())
                    .grossWeightKg(catalog.getGrossWeightKg())
                    .lengthMm(catalog.getLengthMm())
                    .wheelbaseMm(catalog.getWheelbaseMm())
                    .features(catalog.getFeatures())
                    .modelName(catalog.getModel() != null ? catalog.getModel().getName() : null)
                    .versionName(catalog.getVersion() != null ? catalog.getVersion().getName() : null)
                    .build();
        }

        return VehicleDetailResponse.builder()
                .productTitle(product.getTitle())
                .productPrice(product.getPrice())
                .productStatus(product.getStatus().name())
                .brandName(brand != null ? brand.getName() : null)
                .brandLogoUrl(brand != null ? brand.getLogoUrl() : null)
                .modelName(model != null ? model.getName() : null)
                .versionName(version != null ? version.getName() : null)
                .categoryName(category != null ? category.getName() : null)
                .mileageKm(details.getMileageKm())
                .batteryHealthPercent(details.getBatteryHealthPercent())
                .hasRegistration(details.getHasRegistration())
                .hasInsurance(details.getHasInsurance())
                .warrantyMonths(details.getWarrantyMonths())
                .vehicleCatalog(catalogResponse)
                .build();
    }


}
