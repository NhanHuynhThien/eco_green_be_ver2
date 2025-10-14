package com.evdealer.evdealermanagement.repository;

import com.evdealer.evdealermanagement.entity.wishlist.WishlistItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, String> {

    boolean existsByWishlist_IdAndProduct_Id(String wishlistId, String productId);

    @Modifying
    long deleteByWishlist_IdAndProduct_Id(String wishlistId, String productId);

    @EntityGraph(attributePaths = {"product", "product.images"})
    Page<WishlistItem> findByWishlistId(String wishlistId, Pageable pageable);

    @EntityGraph(attributePaths = {"product", "product.images"})
    Page<WishlistItem> findByWishlist_Account_Id(String accountId, Pageable pageable);

    boolean existsByWishlist_Account_IdAndProduct_Id(String accountId, String productId);

    @Query("""
SELECT wi.product.id FROM WishlistItem wi WHERE  wi.wishlist.account.id =:accountId AND wi.product.id IN :productIds
""")
    Set<String> findWishedProductIds(String accountId, Collection<String> productIds);
}