package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.vehicle.brand.VehicleBrandsResponse;
import com.evdealer.evdealermanagement.dto.vehicle.brand.VehicleCategoriesResponse;
import com.evdealer.evdealermanagement.entity.vehicle.VehicleDetails;
import com.evdealer.evdealermanagement.repository.VehicleBrandsRepository;
import com.evdealer.evdealermanagement.repository.VehicleCategoryRepository;
import com.evdealer.evdealermanagement.repository.VehicleDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleDetailsRepository vehicleDetailsRepository;
    private final VehicleCategoryRepository vehicleCategoryRepository;
    private final VehicleBrandsRepository vehicleBrandsRepository;

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

    public List<VehicleBrandsResponse> listAllVehicleBrandsSorted() {
        var all =  vehicleBrandsRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        return all.stream().map(v -> new VehicleBrandsResponse(v.getId(), v.getName())).collect(Collectors.toList());
    }

    public List<VehicleCategoriesResponse> listAllVehicleCategoriesSorted() {
        var all = vehicleCategoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        return all.stream().map(v -> new VehicleCategoriesResponse(v.getId(), v.getName())).collect(Collectors.toList());
    }
}