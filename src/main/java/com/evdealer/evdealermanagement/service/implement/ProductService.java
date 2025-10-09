package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.product.detail.ProductDetail;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.mapper.product.ProductMapper;
import com.evdealer.evdealermanagement.repository.ProductRepository;
import com.evdealer.evdealermanagement.service.contract.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {

    private final ProductRepository productRepository;
    private final VehicleService vehicleService;
    private final BatteryService batteryService;

    @Override
    public List<ProductDetail> getAllProductsWithStatusActive() {
        try {
            log.debug("Fetching all products");
            List<ProductDetail> list = productRepository.findAll()
                    .stream()
                    .filter(product -> product.getStatus() == Product.Status.ACTIVE)
                    .map(ProductMapper::toDetailDto)
                    .toList();

            List<ProductDetail> sortedList = new ArrayList<>(list);
            sortedList.sort(Comparator.comparing(ProductDetail::getCreatedAt));

            return sortedList;
        } catch (Exception e) {
            log.error("Error fetching all products", e);
            return List.of();
        }
    }

    @Override
    public Optional<ProductDetail> getProductById(String id) {
        if (id == null) {
            log.warn("Invalid product ID: null");
            return Optional.empty();
        }
        try {
            log.debug("Fetching product by Long ID: {}", id);
            return productRepository.findById(String.valueOf(id))
                    .map(ProductMapper::toDetailDto);
        } catch (Exception e) {
            log.error("Error fetching product by Long ID: {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public List<ProductDetail> getProductByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            log.warn("Product name is null or empty");
            return List.of();
        }

        try {
            log.debug("Searching products by name: {}", name);

            List<Product> products = productRepository.findTitlesByTitleContainingIgnoreCase(name.trim());

            if (products.isEmpty()) {
                log.debug("No products found with name: {}", name);
                return List.of();
            }

            log.debug("Found {} products with name: {}", products.size(), name);

            return products.stream()
                    .map(ProductMapper::toDetailDto)
                    .toList();

        } catch (Exception e) {
            log.error("Error searching products by name: {}", name, e);
            return List.of();
        }
    }

    @Override
    public List<ProductDetail> getProductByType(String type) {
        if (type == null || type.trim().isEmpty()) {
            log.warn("Product type is null or empty");
            return List.of();
        }

        try {
            log.debug("Fetching products by type: {}", type);
            Product.ProductType enumType = Product.ProductType.valueOf(type.toUpperCase());
            return productRepository.findByType(enumType)
                    .stream()
                    .map(ProductMapper::toDetailDto)
                    .toList();

        } catch (IllegalArgumentException e) {
            log.warn("Invalid product type: {}", type);
            return List.of();
        } catch (Exception e) {
            log.error("Error fetching products by type: {}", type, e);
            return List.of();
        }
    }

    @Override
    public List<ProductDetail> getProductByBrand(String brand) {
        if (brand == null || brand.trim().isEmpty()) {
            log.warn("Brand name is null or empty");
            return List.of();
        }

        try {
            log.debug("Fetching products by brand: {}", brand);

            List<String> allProductIds = getProductIdsByBrand(brand);

            if (allProductIds.isEmpty()) {
                log.debug("No products found for brand: {}", brand);
                return List.of();
            }

            // Filter ACTIVE và sort theo createdAt
            List<ProductDetail> products = productRepository.findAllById(allProductIds)
                    .stream()
                    .filter(product -> product.getStatus() == Product.Status.ACTIVE)
                    .map(ProductMapper::toDetailDto)
                    .toList();

            // Sắp xếp theo createdAt
            List<ProductDetail> sortedList = new ArrayList<>(products);
            sortedList.sort(Comparator.comparing(ProductDetail::getCreatedAt));

            return sortedList;

        } catch (Exception e) {
            log.error("Error fetching products by brand: {}", brand, e);
            return List.of();
        }
    }

    @Override
    public List<ProductDetail> getNewProducts() {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        List<Product> products = productRepository.findTop12ByStatusOrderByCreatedAtDesc(Product.Status.ACTIVE);

        return products.stream()
                .map(ProductDetail::fromEntity)
                .collect(Collectors.toList());
    }

    // ============================================
    // NEW METHOD: Filter với multiple filters
    // ============================================
    public List<ProductDetail> filterProducts(String name, String brand, String type) {
        try {
            log.debug("Filtering products with name: {}, brand: {}, type: {}", name, brand, type);

            // Start with all ACTIVE products
            List<Product> products = productRepository.findAll()
                    .stream()
                    .filter(product -> product.getStatus() == Product.Status.ACTIVE)
                    .collect(Collectors.toList());

            if (products.isEmpty()) {
                log.debug("No active products found");
                return List.of();
            }

            // Apply name filter
            if (name != null && !name.trim().isEmpty()) {
                String searchName = name.trim().toLowerCase();
                products = products.stream()
                        .filter(p -> p.getTitle() != null &&
                                p.getTitle().toLowerCase().contains(searchName))
                        .collect(Collectors.toList());
                log.debug("After name filter '{}': {} products", name, products.size());
            }

            // Apply type filter
            if (type != null && !type.trim().isEmpty()) {
                try {
                    Product.ProductType enumType = Product.ProductType.valueOf(type.trim().toUpperCase());
                    products = products.stream()
                            .filter(p -> p.getType() == enumType)
                            .collect(Collectors.toList());
                    log.debug("After type filter '{}': {} products", type, products.size());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid product type: {}", type);
                    throw e;
                }
            }

            // Apply brand filter
            if (brand != null && !brand.trim().isEmpty()) {
                List<String> productIdsByBrand = getProductIdsByBrand(brand.trim());
                products = products.stream()
                        .filter(p -> productIdsByBrand.contains(p.getId()))
                        .collect(Collectors.toList());
                log.debug("After brand filter '{}': {} products", brand, products.size());
            }

            // Convert to DTO and sort by createdAt
            List<ProductDetail> result = products.stream()
                    .map(ProductMapper::toDetailDto)
                    .collect(Collectors.toList());

            result.sort(Comparator.comparing(ProductDetail::getCreatedAt));

            log.info("Filter completed: {} products found", result.size());
            return result;

        } catch (IllegalArgumentException e) {
            log.error("Invalid filter parameter: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error filtering products with name: {}, brand: {}, type: {}", name, brand, type, e);
            return List.of();
        }
    }

    // ============================================
    // HELPER METHOD: Get product IDs by brand
    // ============================================
    private List<String> getProductIdsByBrand(String brand) {
        try {
            // Get vehicle product IDs
            List<String> vehicleProductIds = vehicleService.getVehicleIdByBrand(brand)
                    .stream()
                    .map(String::valueOf)
                    .toList();

            // Get battery product IDs
            List<String> batteryProductIds = batteryService.getBatteryIdByBrand(brand);

            // Merge and remove duplicates
            List<String> allProductIds = Stream.concat(
                    vehicleProductIds.stream(),
                    batteryProductIds.stream()
            ).distinct().collect(Collectors.toList());

            log.debug("Found {} product IDs for brand '{}'", allProductIds.size(), brand);
            return allProductIds;

        } catch (Exception e) {
            log.error("Error getting product IDs for brand: {}", brand, e);
            return List.of();
        }
    }
}