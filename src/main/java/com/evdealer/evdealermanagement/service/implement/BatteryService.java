package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.battery.brand.BatteryBrandsResponse;
import com.evdealer.evdealermanagement.dto.battery.brand.BatteryTypesResponse;
import com.evdealer.evdealermanagement.dto.battery.brand.BatteryBrandsRequest;
import com.evdealer.evdealermanagement.dto.post.battery.BatteryPostRequest;
import com.evdealer.evdealermanagement.dto.post.battery.BatteryPostResponse;
import com.evdealer.evdealermanagement.dto.post.common.ProductImageResponse;
import com.evdealer.evdealermanagement.entity.battery.BatteryBrands;
import com.evdealer.evdealermanagement.entity.battery.BatteryDetails;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.entity.product.ProductImages;
import com.evdealer.evdealermanagement.exceptions.AppException;
import com.evdealer.evdealermanagement.exceptions.ErrorCode;
import com.evdealer.evdealermanagement.mapper.battery.BatteryMapper;
import com.evdealer.evdealermanagement.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatteryService {

    private final BatteryDetailRepository batteryDetailRepository;
    private final BatteryBrandsRepository batteryBrandsRepository;
    private final BatteryTypesRepository batteryTypesRepository;
    private final ProductRepository productRepository;
    private final PostService postService;
    private final ProductImagesRepository productImagesRepository;

    /**
     * Lấy danh sách Battery Product IDs theo tên sản phẩm
     */
    public List<String> getBatteryIdByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            log.warn("Battery name is null or empty");
            return List.of();
        }

        try {
            log.debug("Getting battery IDs by name: {}", name);
            return batteryDetailRepository.findProductIdsByProductTitle(name);
        } catch (Exception e) {
            log.error("Error getting battery IDs by name: {}", name, e);
            return List.of();
        }
    }

    /**
     * Lấy danh sách Battery Product IDs theo tên hãng
     */
    public List<String> getBatteryIdByBrand(String brand) {
        if (brand == null || brand.trim().isEmpty()) {
            log.warn("Battery brand is null or empty");
            return List.of();
        }

        try {
            log.debug("Getting battery IDs by brand: {}", brand);
            return batteryDetailRepository.findProductIdsByBrandName(brand);
        } catch (Exception e) {
            log.error("Error getting battery IDs by brand: {}", brand, e);
            return List.of();
        }
    }

    /**
     * Lấy BatteryDetails theo tên sản phẩm
     */
    public List<BatteryDetails> getBatteryDetailsByProductName(String name) {
        if (name == null || name.trim().isEmpty()) {
            log.warn("Product name is null or empty");
            return List.of();
        }

        try {
            log.debug("Getting battery details by product name: {}", name);
            return batteryDetailRepository.findByProductTitleLikeIgnoreCase(name);
        } catch (Exception e) {
            log.error("Error getting battery details by product name: {}", name, e);
            return List.of();
        }
    }

    /**
     * Lấy BatteryDetails theo ID
     */
    public Optional<BatteryDetails> getBatteryDetailsById(String id) {
        if (id == null || id.trim().isEmpty()) {
            log.warn("Invalid battery ID: {}", id);
            return Optional.empty();
        }

        try {
            UUID.fromString(id); // Validate UUID format
            log.debug("Getting battery details by ID: {}", id);
            return batteryDetailRepository.findById(id);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format for battery ID: {}", id);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error getting battery details by ID: {}", id, e);
            return Optional.empty();
        }
    }

    /**
     * Lấy tất cả BatteryDetails
     */
    public List<BatteryDetails> getAllBatteryDetails() {
        try {
            log.debug("Getting all battery details");
            return batteryDetailRepository.findAll();
        } catch (Exception e) {
            log.error("Error getting all battery details", e);
            return List.of();
        }
    }

    /**
     * Lấy pin theo battery type
     */
    public List<BatteryDetails> getBatteryDetailsByType(String type) {
        if (type == null || type.trim().isEmpty()) {
            log.warn("Battery type is null or empty");
            return List.of();
        }

        try {
            log.debug("Getting batteries by type: {}", type);
            return batteryDetailRepository.findByBatteryTypeNameLikeIgnoreCase(type);
        } catch (Exception e) {
            log.error("Error getting batteries by type: {}", type, e);
            return List.of();
        }
    }

    /**
     * Lấy pin theo capacity range
     */
    public List<BatteryDetails> getBatteriesByCapacityRange(Double minCapacity, Double maxCapacity) {
        if (minCapacity == null || maxCapacity == null || minCapacity < 0 || maxCapacity < minCapacity) {
            log.warn("Invalid capacity range: {} - {}", minCapacity, maxCapacity);
            return List.of();
        }

        try {
            log.debug("Getting batteries by capacity range: {} - {}", minCapacity, maxCapacity);
            return batteryDetailRepository.findByCapacityKwhBetween(BigDecimal.valueOf(minCapacity),
                    BigDecimal.valueOf(maxCapacity));
        } catch (Exception e) {
            log.error("Error getting batteries by capacity range: {} - {}", minCapacity, maxCapacity, e);
            return List.of();
        }
    }

    /**
     * Lấy pin theo danh sách các hãng
     */
    public List<BatteryDetails> getBatteriesByBrands(List<String> brandNames) {
        if (brandNames == null || brandNames.isEmpty()) {
            log.warn("Brand names list is null or empty");
            return List.of();
        }

        try {
            log.debug("Getting batteries by brands: {}", brandNames);
            return batteryDetailRepository.findByBrandNameIn(brandNames);
        } catch (Exception e) {
            log.error("Error getting batteries by brands: {}", brandNames, e);
            return List.of();
        }
    }

    /**
     * Lấy pin của các hãng phổ biến (Panasonic, Samsung SDI, LG Energy)
     */
    public List<BatteryDetails> getPopularBrandBatteries() {
        List<String> popularBrands = List.of("Panasonic", "Samsung SDI", "LG Energy");
        return getBatteriesByBrands(popularBrands);
    }

    public List<BatteryTypesResponse> listAllBatteryTypesSorted() {
        var all = batteryTypesRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        return all.stream().map(b -> new BatteryTypesResponse(b.getId(), b.getName())).collect(Collectors.toList());
    }

    public List<BatteryBrandsResponse> listAllBatteryBrandsSorted() {
        var all = batteryBrandsRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        return all.stream().map(b -> BatteryBrandsResponse.builder()
                .brandId(b.getId())
                .brandName(b.getName())
                .build())
                .collect(Collectors.toList());
    }

    public List<BatteryBrandsResponse> listAllBatteryNameAndLogo() {
        var all = batteryBrandsRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        return all.stream().map(b -> BatteryBrandsResponse.builder()
                .brandName(b.getName())
                .logoUrl(b.getLogoUrl())
                .build())
                .collect(Collectors.toList());
    }

    public BatteryBrandsResponse addNewBatteryBrand(BatteryBrandsRequest req) {

        String brandName = req.getBrandName().trim();
        if (batteryBrandsRepository.existsByNameIgnoreCase(brandName)) {
            throw new AppException(ErrorCode.BRAND_EXISTS, "Brand name already exists");
        }

        BatteryBrands e = new BatteryBrands();
        e.setName(brandName);
        e.setLogoUrl(req.getLogoUrl());
        e = batteryBrandsRepository.save(e);
        return BatteryMapper.mapToBatteryBrandsResponse(e);
    }

    public BatteryPostResponse updateBatteryPost(String productId, BatteryPostRequest request,
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
        product.setUpdatedAt(LocalDateTime.now());

        BatteryDetails details = batteryDetailRepository.findByProductId(product.getId());
        details.setProduct(product);
        details.setBrand(batteryBrandsRepository.findById(request.getBrandId())
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND)));
        details.setBatteryType(batteryTypesRepository.findById(request.getBatteryTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.TYPE_NOT_FOUND)));
        details.setCapacityKwh(request.getCapacityKwh());
        details.setVoltageV(request.getVoltageV());

        if(images != null && !images.isEmpty()) {

            productImagesRepository.deleteAllByProduct(product);

            List<ProductImageResponse> imageDtos = postService.uploadAndSaveImages(product, images, imagesMetaJson);

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
        batteryDetailRepository.save(details);

        return BatteryPostResponse.builder()
                .productId(product.getId())
                .status(product.getStatus().name())
                .title(product.getTitle())
                .description(product.getDescription())
                .price(product.getPrice())
                .city(product.getCity())
                .district(product.getDistrict())
                .ward(product.getWard())
                .addressDetail(product.getAddressDetail())
                .createdAt(product.getCreatedAt())
                .brandId(request.getBrandId())
                .brandName(details.getBrand() != null ? details.getBrand().getName() : null)
                .batteryTypeName(details.getBatteryType() != null ? details.getBatteryType().getName() : null)
                .capacityKwh(details.getCapacityKwh())
                .healthPercent(details.getHealthPercent())
                .voltageV(details.getVoltageV())
                .build();
    }
}