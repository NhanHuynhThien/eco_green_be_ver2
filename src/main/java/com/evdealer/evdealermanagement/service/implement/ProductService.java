package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.product.detail.ProductDetail;
import com.evdealer.evdealermanagement.dto.product.moderation.ProductPendingResponse;
import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.mapper.product.ProductMapper;
import com.evdealer.evdealermanagement.repository.ProductRepository;
import com.evdealer.evdealermanagement.service.contract.IProductService;
import com.evdealer.evdealermanagement.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final WishlistService wishlistService;

    @Override
    @Transactional(readOnly = true)
    public List<ProductDetail> getAllProductsWithStatusActive() {
        try {
            log.info("=== START getAllProductsWithStatusActive ===");

            String accountId = SecurityUtils.getCurrentAccountId();
            log.info("Current accountId: {}", accountId);

            // Get ACTIVE products
            List<Product> products = productRepository.findAll().stream()
                    .filter(p -> p.getStatus() == Product.Status.ACTIVE)
                    .collect(Collectors.toList());

            log.info("Found {} ACTIVE products from DB", products.size());

            if (products.isEmpty()) {
                log.warn("No active products in database!");
                return List.of();
            }

            // Attach isWishlisted + map to ProductDetail with error handling
            List<ProductDetail> result;
            try {
                result = wishlistService.attachWishlistFlag(
                        accountId,
                        products,
                        ProductMapper::toDetailDto,
                        ProductDetail::setIsWishlisted);
                log.info("Successfully mapped {} products to DTOs", result.size());
            } catch (Exception e) {
                log.error("Error in wishlistService.attachWishlistFlag, falling back to basic mapping", e);
                // Fallback: Map without wishlist
                result = products.stream()
                        .map(ProductMapper::toDetailDto)
                        .collect(Collectors.toList());
            }

            // Sort with null-safe comparator
            result.sort(Comparator.comparing(
                    ProductDetail::getCreatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder())));

            log.info("=== END getAllProductsWithStatusActive: {} products ===", result.size());
            return result;

        } catch (Exception e) {
            log.error("FATAL ERROR in getAllProductsWithStatusActive", e);
            throw new RuntimeException("Failed to get all active products: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductDetail> getProductById(String id) {
        if (id == null || id.trim().isEmpty()) {
            log.warn("Invalid product ID: null or empty");
            return Optional.empty();
        }

        try {
            log.info("Fetching product by ID: {}", id);

            String accountId = SecurityUtils.getCurrentAccountId();
            Optional<Product> productOpt = productRepository.findById(id);

            if (productOpt.isEmpty()) {
                log.info("Product not found with ID: {}", id);
                return Optional.empty();
            }

            ProductDetail productDetail = ProductMapper.toDetailDto(productOpt.get());

            // If user logged in, check if this product is in wishlist
            if (accountId != null) {
                try {
                    boolean isWishlisted = wishlistService.isProductInWishlist(accountId, id);
                    productDetail.setIsWishlisted(isWishlisted);
                } catch (Exception e) {
                    log.error("Error checking wishlist status for product {}", id, e);
                    productDetail.setIsWishlisted(false);
                }
            }

            return Optional.of(productDetail);

        } catch (Exception e) {
            log.error("Error fetching product by ID: {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDetail> getProductByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            log.warn("Product name is null or empty");
            return List.of();
        }

        try {
            log.info("Searching products by name: {}", name);

            String accountId = SecurityUtils.getCurrentAccountId();
            List<Product> products = productRepository.findTitlesByTitleContainingIgnoreCase(name.trim());

            if (products.isEmpty()) {
                log.info("No products found with name: {}", name);
                return List.of();
            }

            List<ProductDetail> result;
            try {
                result = wishlistService.attachWishlistFlag(
                        accountId,
                        products,
                        ProductMapper::toDetailDto,
                        ProductDetail::setIsWishlisted);
            } catch (Exception e) {
                log.error("Error attaching wishlist flags, using basic mapping", e);
                result = products.stream()
                        .map(ProductMapper::toDetailDto)
                        .collect(Collectors.toList());
            }

            log.info("Found {} products matching name: {}", result.size(), name);
            return result;

        } catch (Exception e) {
            log.error("Error searching products by name: {}", name, e);
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDetail> getProductByType(String type) {
        if (type == null || type.trim().isEmpty()) {
            log.warn("Product type is null or empty");
            return List.of();
        }

        try {
            log.info("Fetching products by type: {}", type);

            Product.ProductType enumType;
            try {
                enumType = Product.ProductType.valueOf(type.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid product type: {}", type);
                return List.of();
            }

            String accountId = SecurityUtils.getCurrentAccountId();
            List<Product> products = productRepository.findByType(enumType);

            if (products.isEmpty()) {
                log.info("No products found with type: {}", type);
                return List.of();
            }

            List<ProductDetail> result;
            try {
                result = wishlistService.attachWishlistFlag(
                        accountId,
                        products,
                        ProductMapper::toDetailDto,
                        ProductDetail::setIsWishlisted);
            } catch (Exception e) {
                log.error("Error attaching wishlist flags, using basic mapping", e);
                result = products.stream()
                        .map(ProductMapper::toDetailDto)
                        .collect(Collectors.toList());
            }

            log.info("Found {} products with type: {}", result.size(), type);
            return result;

        } catch (Exception e) {
            log.error("Error fetching products by type: {}", type, e);
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDetail> getProductByBrand(String brand) {
        if (brand == null || brand.trim().isEmpty()) {
            log.warn("Brand name is null or empty");
            return List.of();
        }

        try {
            log.info("Fetching products by brand: {}", brand);

            List<String> allProductIds = getProductIdsByBrand(brand);

            if (allProductIds.isEmpty()) {
                log.info("No products found for brand: {}", brand);
                return List.of();
            }

            // Filter ACTIVE and sort by createdAt
            List<Product> products = productRepository.findAllById(allProductIds)
                    .stream()
                    .filter(product -> product.getStatus() == Product.Status.ACTIVE)
                    .collect(Collectors.toList());

            if (products.isEmpty()) {
                log.info("No ACTIVE products found for brand: {}", brand);
                return List.of();
            }

            String accountId = SecurityUtils.getCurrentAccountId();
            List<ProductDetail> result;

            try {
                result = wishlistService.attachWishlistFlag(
                        accountId,
                        products,
                        ProductMapper::toDetailDto,
                        ProductDetail::setIsWishlisted);
            } catch (Exception e) {
                log.error("Error attaching wishlist flags, using basic mapping", e);
                result = products.stream()
                        .map(ProductMapper::toDetailDto)
                        .collect(Collectors.toList());
            }

            // Sort by createdAt with null-safe comparator
            result.sort(Comparator.comparing(
                    ProductDetail::getCreatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder())));

            log.info("Found {} products for brand: {}", result.size(), brand);
            return result;

        } catch (Exception e) {
            log.error("Error fetching products by brand: {}", brand, e);
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDetail> getNewProducts() {
        try {
            log.info("=== START getNewProducts ===");

            String accountId = SecurityUtils.getCurrentAccountId();

            List<Product> products = productRepository.findTop12ByStatusOrderByCreatedAtDesc(Product.Status.ACTIVE);

            log.info("Found {} new products from DB", products.size());

            if (products.isEmpty()) {
                log.warn("No new products found");
                return List.of();
            }

            List<ProductDetail> result;
            try {
                result = wishlistService.attachWishlistFlag(
                        accountId,
                        products,
                        ProductMapper::toDetailDto,
                        ProductDetail::setIsWishlisted);
            } catch (Exception e) {
                log.error("Error attaching wishlist flags, using basic mapping", e);
                result = products.stream()
                        .map(ProductMapper::toDetailDto)
                        .collect(Collectors.toList());
            }

            log.info("=== END getNewProducts: {} products ===", result.size());
            return result;

        } catch (Exception e) {
            log.error("FATAL ERROR in getNewProducts", e);
            throw new RuntimeException("Failed to get new products: " + e.getMessage(), e);
        }
    }

    // ============================================
    // NEW METHOD: Filter with multiple filters
    // ============================================
    @Transactional(readOnly = true)
    public List<ProductDetail> filterProducts(String name, String brand, String type) {
        try {
            log.info("=== START filterProducts: name={}, brand={}, type={} ===", name, brand, type);

            // Start with all ACTIVE products
            List<Product> products = productRepository.findAll()
                    .stream()
                    .filter(product -> product.getStatus() == Product.Status.ACTIVE)
                    .collect(Collectors.toList());

            log.info("Initial ACTIVE products count: {}", products.size());

            if (products.isEmpty()) {
                log.warn("No active products found in database");
                return List.of();
            }

            // Apply name filter
            if (name != null && !name.trim().isEmpty()) {
                String searchName = name.trim().toLowerCase();
                products = products.stream()
                        .filter(p -> p.getTitle() != null &&
                                p.getTitle().toLowerCase().contains(searchName))
                        .collect(Collectors.toList());
                log.info("After name filter '{}': {} products", name, products.size());
            }

            // Apply type filter
            if (type != null && !type.trim().isEmpty()) {
                try {
                    Product.ProductType enumType = Product.ProductType.valueOf(type.trim().toUpperCase());
                    products = products.stream()
                            .filter(p -> p.getType() == enumType)
                            .collect(Collectors.toList());
                    log.info("After type filter '{}': {} products", type, products.size());
                } catch (IllegalArgumentException e) {
                    log.error("Invalid product type: {}", type);
                    throw new IllegalArgumentException("Invalid product type: " + type);
                }
            }

            // Apply brand filter
            if (brand != null && !brand.trim().isEmpty()) {
                List<String> productIdsByBrand = getProductIdsByBrand(brand.trim());
                products = products.stream()
                        .filter(p -> productIdsByBrand.contains(p.getId()))
                        .collect(Collectors.toList());
                log.info("After brand filter '{}': {} products", brand, products.size());
            }

            if (products.isEmpty()) {
                log.info("No products match the filter criteria");
                return List.of();
            }

            String accountId = SecurityUtils.getCurrentAccountId();
            List<ProductDetail> result;

            try {
                result = wishlistService.attachWishlistFlag(
                        accountId,
                        products,
                        ProductMapper::toDetailDto,
                        ProductDetail::setIsWishlisted);
            } catch (Exception e) {
                log.error("Error attaching wishlist flags, using basic mapping", e);
                result = products.stream()
                        .map(ProductMapper::toDetailDto)
                        .collect(Collectors.toList());
            }

            // Sort by createdAt with null-safe comparator
            result.sort(Comparator.comparing(
                    ProductDetail::getCreatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder())));

            log.info("=== END filterProducts: {} products found ===", result.size());
            return result;

        } catch (IllegalArgumentException e) {
            log.error("Invalid filter parameter: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("FATAL ERROR in filterProducts", e);
            throw new RuntimeException("Failed to filter products: " + e.getMessage(), e);
        }
    }

    // ============================================
    // HELPER METHOD: Get product IDs by brand
    // ============================================
    private List<String> getProductIdsByBrand(String brand) {
        try {
            log.debug("Getting product IDs for brand: {}", brand);

            // Get vehicle product IDs
            List<String> vehicleProductIds = vehicleService.getVehicleIdByBrand(brand)
                    .stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList());

            // Get battery product IDs
            List<String> batteryProductIds = batteryService.getBatteryIdByBrand(brand);

            // Merge and remove duplicates
            List<String> allProductIds = Stream.concat(
                    vehicleProductIds.stream(),
                    batteryProductIds.stream()).distinct().collect(Collectors.toList());

            log.debug("Found {} product IDs for brand '{}' (vehicles: {}, batteries: {})",
                    allProductIds.size(), brand, vehicleProductIds.size(), batteryProductIds.size());

            return allProductIds;

        } catch (Exception e) {
            log.error("Error getting product IDs for brand: {}", brand, e);
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public List<ProductPendingResponse> getPendingProducts() {
        List<Product> products = productRepository.findByStatus(Product.Status.PENDING_REVIEW);
        List<ProductPendingResponse> result = new ArrayList<>();

        if (products.isEmpty()) {
            return result;
        }

        for (Product p : products) {
            Account seller = p.getSeller();
            String imageUrl = null;

            if (p.getImages() != null && !p.getImages().isEmpty()) {
                imageUrl = p.getImages().get(0).getImageUrl();
            }

            ProductPendingResponse dto = new ProductPendingResponse();
            dto.setId(p.getId());
            dto.setTitle(p.getTitle());
            dto.setType(p.getType() != null ? p.getType().name() : null);
            dto.setPrice(p.getPrice());
            dto.setImageUrl(imageUrl);

            if (seller != null) {
                dto.setSellerId(seller.getId());
                dto.setSellerName(seller.getFullName());
                dto.setSellerPhone(seller.getPhone());
            }
            dto.setCreatedAt(p.getCreatedAt());
            result.add(dto);
        }

        return result;
    }

    public List<String> getAllStatuses() {
        List<String> statuses = new ArrayList<>();
        for (Product.Status status : Product.Status.values()) {
            statuses.add(status.name());
        }
        return statuses;
    }
}