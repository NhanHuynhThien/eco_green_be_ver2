package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.wishlist.WishlistItemResponse;
import com.evdealer.evdealermanagement.dto.wishlist.WishlistPageResponse;
import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.entity.wishlist.Wishlist;
import com.evdealer.evdealermanagement.entity.wishlist.WishlistItem;
import com.evdealer.evdealermanagement.exceptions.AppException;
import com.evdealer.evdealermanagement.exceptions.ErrorCode;
import com.evdealer.evdealermanagement.mapper.wishlist.WishlistMapper;
import com.evdealer.evdealermanagement.repository.ProductRepository;
import com.evdealer.evdealermanagement.repository.WishlistItemRepository;
import com.evdealer.evdealermanagement.repository.WishlistRepository;
import com.evdealer.evdealermanagement.service.contract.IWishlistService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WishlistService implements IWishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final EntityManager em;

    @Override
    public void addWishlistItem(String accountId, String productId) {
        log.debug("Adding wishlist item for account: {}, product: {}", accountId, productId);

        // Validate UUID format
        validateUuid(accountId, "accountId");
        validateUuid(productId, "productId");

        // Check if product exists
        if (!productRepository.existsById(productId)) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found with ID: " + productId);
        }

        // Get or create Wishlist for the account
        Wishlist wishlist = wishlistRepository.findByAccountId(accountId)
                .orElseGet(() -> {
                    log.debug("Creating new wishlist for account: {}", accountId);
                    Wishlist newWishlist = Wishlist.builder()
                            .account(em.getReference(Account.class, accountId))
                            .build();
                    return wishlistRepository.save(newWishlist);
                });

        // Check if item already exists
        if (wishlistItemRepository.existsByWishlist_IdAndProduct_Id(wishlist.getId(), productId)) {
            log.debug("Wishlist item already exists for wishlist: {}, product: {}", wishlist.getId(), productId);
            return;
        }

        // Create and save WishlistItem
        WishlistItem item = WishlistItem.builder()
                .wishlist(em.getReference(Wishlist.class, wishlist.getId()))
                .product(em.getReference(Product.class, productId))
                .build();

        wishlistItemRepository.save(item);
        log.debug("Wishlist item added successfully");
    }

    @Override
    public void removeWishlistItem(String accountId, String productId) {
        log.debug("Removing wishlist item for account: {}, product: {}", accountId, productId);

        // Validate UUID format
        validateUuid(accountId, "accountId");
        validateUuid(productId, "productId");

        // Get Wishlist ID
        String wishlistId = wishlistRepository.findByAccountId(accountId)
                .map(Wishlist::getId)
                .orElseThrow(() -> new AppException(ErrorCode.WISHLIST_NOT_FOUND, "Wishlist not found for account: " + accountId));

        // Delete the item
        long deletedCount = wishlistItemRepository.deleteByWishlist_IdAndProduct_Id(wishlistId, productId);
        if (deletedCount == 0) {
            log.warn("No wishlist item found to remove for wishlist: {}, product: {}", wishlistId, productId);
        } else {
            log.debug("Wishlist item removed successfully, deleted count: {}", deletedCount);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public WishlistPageResponse<WishlistItemResponse> listWishlistItem(String accountId, Pageable pageable) {
        log.debug("Listing wishlist items for account: {}, page: {}, size: {}", accountId, pageable.getPageNumber(), pageable.getPageSize());

        // Validate UUID format
        validateUuid(accountId, "accountId");

        // Fetch page of wishlist items
        Page<WishlistItem> page = wishlistItemRepository.findByWishlist_Account_Id(accountId, pageable);
        return WishlistPageResponse.fromPage(page, WishlistMapper::mapToWishlistItemResponse);
    }

    private void validateUuid(String id, String fieldName) {
        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_KEY, String.format("Invalid UUID format for %s: %s", fieldName, id));
        }
    }

    //Function<Product, D> toDto is functional interface , it gets a Product object and map into D (dto).
    //BiConsumer<D, Boolean> setIsWishlisted is also function interface. It gets 2 parameter D and Boolean in order to do to assign isWishlisted flag
    @Transactional(readOnly = true)
    public <D> List<D> attachWishlistFlag(String accountId, List<Product> products, Function<Product, D> toDto, BiConsumer<D, Boolean> setIsWishlisted) {
        if(products == null || products.isEmpty()) {
            return List.of();
        }
        //If not logged in then all is false
        if(accountId == null || accountId.isEmpty()) {
            return products.stream().map(p -> {
                D dto = toDto.apply(p);
                setIsWishlisted.accept(dto, false);
                return dto;
            }).collect(Collectors.toList());
        }
        //Get list of productId that user is wishlisted.
        List<String> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
        Set<String> wishedIds = wishlistItemRepository.findWishedProductIds(accountId, productIds);
        //Map to DTO and flag isWishlisted
        return products.stream().map(p -> {
            D dto = toDto.apply(p);
            setIsWishlisted.accept(dto, wishedIds.contains(p.getId()));
            return dto;
        }).collect(Collectors.toList());
    }

    public boolean isProductInWishlist(String accountId, String productId) {
        return wishlistItemRepository.existsByWishlist_Account_IdAndProduct_Id(accountId, productId);
    }
}