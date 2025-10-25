package com.evdealer.evdealermanagement.repository;

import com.evdealer.evdealermanagement.entity.product.Product;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {
    List<Product> findByType(Product.ProductType type);

    boolean existsById(@NotNull String productId);

    @Query("SELECT p FROM Product p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Product> findTitlesByTitleContainingIgnoreCase(@Param("title") String title, Pageable pageable);

    List<Product> findTop12ByStatusOrderByCreatedAtDesc(Product.Status status);

    Optional<Product> findById(@NotNull String productId);

    List<Product> findByStatus(Product.Status status);

    Page<Product> findByStatus(Product.Status status, Pageable pageable);

    Page<Product> findByStatusAndType(Product.Status status, Product.ProductType type, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.seller.id = :sellerId AND p.status = :status ORDER BY p.createdAt DESC")
    List<Product> findBySellerAndStatus(@Param("sellerId") String sellerId, @Param("status") Product.Status status);

    Optional<Product> findByIdAndSellerId(String id, String sellerId);

    long countBySeller_Id(String sellerId);
}