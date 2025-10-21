package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.post.common.ProductImageResponse;
import com.evdealer.evdealermanagement.dto.post.vehicle.VehiclePostRequest;
import com.evdealer.evdealermanagement.dto.post.vehicle.VehiclePostResponse;
import com.evdealer.evdealermanagement.dto.vehicle.brand.VehicleBrandsRequest;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.evdealer.evdealermanagement.dto.vehicle.brand.VehicleBrandsResponse;
import com.evdealer.evdealermanagement.dto.vehicle.brand.VehicleCategoriesResponse;
import com.evdealer.evdealermanagement.dto.vehicle.catalog.VehicleCatalogResponse;
import com.evdealer.evdealermanagement.dto.vehicle.detail.VehicleDetailResponse;
import com.evdealer.evdealermanagement.dto.vehicle.model.VehicleModelRequest;
import com.evdealer.evdealermanagement.dto.vehicle.model.VehicleModelResponse;
import com.evdealer.evdealermanagement.dto.vehicle.model.VehicleModelVersionRequest;
import com.evdealer.evdealermanagement.dto.vehicle.model.VehicleModelVersionResponse;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.entity.product.ProductImages;
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

import java.time.LocalDateTime;
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
    private final ProductImagesRepository productImagesRepository;
    private final PostService postService;
    private final VehicleDetailResponse vehicleDetailResponse;

    /**
     * Lấy danh sách Vehicle Product IDs theo tên sản phẩm
     */
    public List<Long> getVehicleIdByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            log.warn("Vehicle name is null or empty");
            return List.of();
        }

        try {
            log.debug("Getting vehicle IDs by name: {}", name);
            return vehicleDetailsRepository.findVehicleProductIdsByName(name);
        } catch (Exception e) {
            log.error("Error getting vehicle IDs by name: {}", name, e);
            return List.of();
        }
    }

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

    /**
     * Lấy VehicleDetails theo tên sản phẩm
     */
    public List<VehicleDetails> getVehicleDetailsByProductName(String name) {
        if (name == null || name.trim().isEmpty()) {
            log.warn("Product name is null or empty");
            return List.of();
        }

        try {
            log.debug("Getting vehicle details by product name: {}", name);
            return vehicleDetailsRepository.findVehicleDetailsByProductName(name);
        } catch (Exception e) {
            log.error("Error getting vehicle details by product name: {}", name, e);
            return List.of();
        }
    }

    /**
     * Lấy VehicleDetails theo ID
     */
    public Optional<VehicleDetails> getVehicleDetailsById(Long id) {
        if (id == null || id <= 0) {
            log.warn("Invalid vehicle ID: {}", id);
            return Optional.empty();
        }

        try {
            log.debug("Getting vehicle details by ID: {}", id);
            return vehicleDetailsRepository.findById(String.valueOf(id));
        } catch (Exception e) {
            log.error("Error getting vehicle details by ID: {}", id, e);
            return Optional.empty();
        }
    }

    /**
     * Lấy tất cả VehicleDetails
     */
    public List<VehicleDetails> getAllVehicleDetails() {
        try {
            log.debug("Getting all vehicle details");
            return vehicleDetailsRepository.findAll();
        } catch (Exception e) {
            log.error("Error getting all vehicle details", e);
            return List.of();
        }
    }

    /**
     * Lấy xe theo model
     */
    public List<VehicleDetails> getVehiclesByModel(String model) {
        if (model == null || model.trim().isEmpty()) {
            log.warn("Vehicle model is null or empty");
            return List.of();
        }

        try {
            log.debug("Getting vehicles by model: {}", model);
            return vehicleDetailsRepository.findVehiclesByModel(model);
        } catch (Exception e) {
            log.error("Error getting vehicles by model: {}", model, e);
            return List.of();
        }
    }

    /**
     * Lấy xe theo năm sản xuất
     */
    public List<VehicleDetails> getVehiclesByYear(Integer year) {
        if (year == null || year <= 0) {
            log.warn("Invalid year: {}", year);
            return List.of();
        }

        try {
            log.debug("Getting vehicles by year: {}", year);
            return vehicleDetailsRepository.findVehiclesByYear(year);
        } catch (Exception e) {
            log.error("Error getting vehicles by year: {}", year, e);
            return List.of();
        }
    }

    /**
     * Lấy xe theo category
     */
    public List<VehicleDetails> getVehiclesByCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            log.warn("Category name is null or empty");
            return List.of();
        }

        try {
            log.debug("Getting vehicles by category: {}", categoryName);
            return vehicleDetailsRepository.findVehiclesByCategory(categoryName);
        } catch (Exception e) {
            log.error("Error getting vehicles by category: {}", categoryName, e);
            return List.of();
        }
    }

    /**
     * Lấy xe theo price range
     */
    public List<VehicleDetails> getVehiclesByPriceRange(Double minPrice, Double maxPrice) {
        if (minPrice == null || maxPrice == null || minPrice < 0 || maxPrice < minPrice) {
            log.warn("Invalid price range: {} - {}", minPrice, maxPrice);
            return List.of();
        }

        try {
            log.debug("Getting vehicles by price range: {} - {}", minPrice, maxPrice);
            return vehicleDetailsRepository.findVehiclesByPriceRange(minPrice, maxPrice);
        } catch (Exception e) {
            log.error("Error getting vehicles by price range: {} - {}", minPrice, maxPrice, e);
            return List.of();
        }
    }

    /**
     * Lấy xe có pin tháo rời
     */
    public List<VehicleDetails> getVehiclesWithRemovableBattery() {
        try {
            log.debug("Getting vehicles with removable battery");
            return vehicleDetailsRepository.findVehiclesWithRemovableBattery();
        } catch (Exception e) {
            log.error("Error getting vehicles with removable battery", e);
            return List.of();
        }
    }

    /**
     * Lấy xe theo tình trạng sức khỏe pin tối thiểu
     */
    public List<VehicleDetails> getVehiclesByBatteryHealth(Integer minHealth) {
        if (minHealth == null || minHealth < 0 || minHealth > 100) {
            log.warn("Invalid battery health: {}", minHealth);
            return List.of();
        }

        try {
            log.debug("Getting vehicles by battery health >= {}", minHealth);
            return vehicleDetailsRepository.findVehiclesByBatteryHealth(minHealth);
        } catch (Exception e) {
            log.error("Error getting vehicles by battery health: {}", minHealth, e);
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



    public VehiclePostResponse updateVehiclePost(String productId, VehiclePostRequest request,
                                                 List<MultipartFile> images, String imagesMetaJson) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if(product.getStatus() != Product.Status.DRAFT) {
            throw new AppException(ErrorCode.PRODUCT_NOT_DRAFT);
        }

        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCity(request.getCity());
        product.setDistrict(request.getDistrict());
        product.setWard(request.getWard());
        product.setAddressDetail(request.getAddressDetail());
        product.setManufactureYear(request.getYear());
        product.setUpdatedAt(LocalDateTime.now());

        VehicleDetails details = vehicleDetailsRepository.findByProductId(product.getId())
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));
        details.setProduct(product);
        details.setBrand(vehicleBrandsRepository.findById(request.getBrandId())
        .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND)));
        details.setCategory(vehicleCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND)));
        details.setModel(vmRepository.findById(request.getModelId())
                .orElseThrow(() -> new AppException(ErrorCode.MODEL_NOT_FOUND)));
        details.setVersion(vmvRepository.findById(request.getVersionId())
                .orElseThrow(() -> new AppException(ErrorCode.VERSION_NOT_FOUND)));
        details.setMileageKm(request.getMileageKm());
        details.setBatteryHealthPercent(request.getBatteryHealthPercent());

        if(images != null && !images.isEmpty()) {

            //xóa ảnh cũ trong database
            productImagesRepository.deleteAllByProduct(product);

            List<ProductImageResponse> imageDtos = postService.uploadAndSaveImages(product, images,  imagesMetaJson);

            product.setImages(
                    imageDtos.stream()
                            .map(dto -> ProductImages.builder()
                                    .product(product)
                                    .imageUrl(dto.getUrl())
                                    .isPrimary(dto.isPrimary())
                                    .build())
                            .toList()
            );
        }

        productRepository.save(product);
        vehicleDetailsRepository.save(details);

        return VehiclePostResponse.builder()
                .productId(product.getId())
                .status(product.getStatus().name())
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .city(request.getCity())
                .district(request.getDistrict())
                .ward(request.getWard())
                .addressDetail(request.getAddressDetail())
                .createdAt(LocalDateTime.now())
                .brandId(request.getBrandId())
                .categoryId(request.getCategoryId())
                .batteryHealthPercent(request.getBatteryHealthPercent())
                .mileageKm(request.getMileageKm())
                .build();
    }
}
