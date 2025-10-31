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

    @Query("""
    SELECT p FROM Product p
    WHERE p.status = :status
    AND EXISTS (
        SELECT 1 FROM PostPayment pay WHERE pay.product = p
    )
    ORDER BY
        (SELECT 
            CASE 
                WHEN pkg.code = 'SPECIAL' THEN 0
                WHEN pkg.code = 'PRIORITY' THEN 1
                WHEN pkg.code = 'STANDARD' THEN 2
                ELSE 3
            END
         FROM PostPayment pay2
         JOIN pay2.postPackage pkg
         WHERE pay2.product = p
         AND pay2.createdAt = (
             SELECT MAX(pp.createdAt)
             FROM PostPayment pp
             WHERE pp.product = p
         )
        ) ASC,
        p.createdAt DESC
""")
    List<Product> findTop12ByStatusOrderByCreatedAtDesc(Product.Status status, Pageable pageable);

    Optional<Product> findById(@NotNull String productId);

    List<Product> findByStatus(Product.Status status);

    Page<Product> findByStatus(Product.Status status, Pageable pageable);

    Page<Product> findByStatusAndType(Product.Status status, Product.ProductType type, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.seller.id = :sellerId AND p.status = :status ORDER BY p.createdAt DESC")
    List<Product> findBySellerAndStatus(@Param("sellerId") String sellerId, @Param("status") Product.Status status);

    Optional<Product> findByIdAndSellerId(String id, String sellerId);

    long countBySeller_Id(String sellerId);

    long countByStatus(Product.Status status);

    @Query("""
        SELECT p FROM Product p
        WHERE p.status = 'ACTIVE'
          AND LOWER(p.title) LIKE LOWER(CONCAT('%', :name, '%'))
        ORDER BY p.isHot DESC, p.createdAt DESC
    """)
    Page<Product> findByNameOrderByHotFirst(String name, Pageable pageable);
}