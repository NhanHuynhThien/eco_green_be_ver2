package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.battery.brand.BatteryBrandsResponse;
import com.evdealer.evdealermanagement.dto.battery.brand.BatteryTypesResponse;
import com.evdealer.evdealermanagement.dto.vehicle.brand.BatteryBrandsRequest;
import com.evdealer.evdealermanagement.entity.battery.BatteryBrands;
import com.evdealer.evdealermanagement.entity.battery.BatteryDetails;
import com.evdealer.evdealermanagement.exceptions.AppException;
import com.evdealer.evdealermanagement.exceptions.ErrorCode;
import com.evdealer.evdealermanagement.mapper.battery.BatteryMapper;
import com.evdealer.evdealermanagement.repository.BatteryBrandsRepository;
import com.evdealer.evdealermanagement.repository.BatteryDetailRepository;
import com.evdealer.evdealermanagement.repository.BatteryTypesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
<<<<<<< HEAD
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
=======
                        .brandName(b.getName())
                        .logoUrl(b.getLogoUrl())
                        .build())
                .collect(Collectors.toList());
    }
>>>>>>> e5ba1b09714b2fd34b9fb547a43286fdd439af02
}