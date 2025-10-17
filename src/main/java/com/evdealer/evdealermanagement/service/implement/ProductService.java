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
    private final WishlistService wishlistService;

    @Override
    @Transactional(readOnly = true)
    public List<ProductDetail> getAllProductsWithStatusActive() {
        try {
            String accountId = SecurityUtils.getCurrentAccountId();

            // Get product list Active
            List<Product> products = productRepository.findAll().stream()
                    .filter(p -> p.getStatus() == Product.Status.ACTIVE).toList();
            // Attach isWishlisted + mapp to ProductDetail
            List<ProductDetail> result = wishlistService.attachWishlistFlag(
                    accountId, products, ProductMapper::toDetailDto,
                    ProductDetail::setIsWishlisted);

            result.sort(Comparator.comparing(ProductDetail::getCreatedAt));

            return result;
        } catch (Exception e) {
            log.error("Error fetching all products", e);
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductDetail> getProductById(String id) {
        if (id == null) {
            log.warn("Invalid product ID: null");
            return Optional.empty();
        }
        try {
            log.debug("Fetching product by Long ID: {}", id);

            String accountId = SecurityUtils.getCurrentAccountId();
            Optional<ProductDetail> result = productRepository.findById(id).map(ProductMapper::toDetailDto);
            // If user logged , check if this product is in wishlist
            if (accountId != null && result.isPresent()) {
                boolean isWishlisted = wishlistService.isProductInWishlist(accountId, id);
                result.get().setIsWishlisted(isWishlisted);
            }

            return result;
        } catch (Exception e) {
            log.error("Error fetching product by Long ID: {}", id, e);
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
            log.debug("Searching products by name: {}", name);

            String accountId = SecurityUtils.getCurrentAccountId();
            List<Product> products = productRepository.findTitlesByTitleContainingIgnoreCase(name.trim());

            if (products.isEmpty()) {
                log.debug("No products found with name: {}", name);
                return List.of();
            }
            return wishlistService.attachWishlistFlag(
                    accountId,
                    products,
                    ProductMapper::toDetailDto,
                    ProductDetail::setIsWishlisted);

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
            log.debug("Fetching products by type: {}", type);
            String accountId = SecurityUtils.getCurrentAccountId();
            Product.ProductType enumType = Product.ProductType.valueOf(type.toUpperCase());
            List<Product> products = productRepository.findByType(enumType);

            return wishlistService.attachWishlistFlag(
                    accountId,
                    products,
                    ProductMapper::toDetailDto,
                    ProductDetail::setIsWishlisted);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid product type: {}", type);
            return List.of();
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
            log.debug("Fetching products by brand: {}", brand);

            List<String> allProductIds = getProductIdsByBrand(brand);

            if (allProductIds.isEmpty()) {
                log.debug("No products found for brand: {}", brand);
                return List.of();
            }

            // Filter ACTIVE và sort theo createdAt
            List<Product> products = productRepository.findAllById(allProductIds)
                    .stream()
                    .filter(product -> product.getStatus() == Product.Status.ACTIVE)
                    .toList();

            String accountId = SecurityUtils.getCurrentAccountId();
            List<ProductDetail> list = wishlistService.attachWishlistFlag(
                    accountId,
                    products,
                    ProductMapper::toDetailDto,
                    ProductDetail::setIsWishlisted);

            // Sắp xếp theo createdAt
            list.sort(Comparator.comparing(ProductDetail::getCreatedAt));

            return list;

        } catch (Exception e) {
            log.error("Error fetching products by brand: {}", brand, e);
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDetail> getNewProducts() {
        try {
            log.debug("Fetching new products");
            String accountId = SecurityUtils.getCurrentAccountId();
            LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

            List<Product> products = productRepository.findTop12ByStatusOrderByCreatedAtDesc(Product.Status.ACTIVE);

            return wishlistService.attachWishlistFlag(
                    accountId,
                    products,
                    ProductMapper::toDetailDto, // Product -> ProductDetail
                    ProductDetail::setIsWishlisted // gắn cờ
            );
        } catch (Exception e) {
            log.error("Error fetching new products", e);
            return List.of();
        }
    }

    // ============================================
    // NEW METHOD: Filter với multiple filters
    // ============================================
    @Transactional(readOnly = true)
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

            String accountId = SecurityUtils.getCurrentAccountId();
            List<ProductDetail> result = wishlistService.attachWishlistFlag(
                    accountId,
                    products,
                    ProductMapper::toDetailDto,
                    ProductDetail::setIsWishlisted);

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
                    batteryProductIds.stream()).distinct().collect(Collectors.toList());

            log.debug("Found {} product IDs for brand '{}'", allProductIds.size(), brand);
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
}