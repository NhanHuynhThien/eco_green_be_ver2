package com.evdealer.evdealermanagement.repository;

import com.evdealer.evdealermanagement.entity.product.Product;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByType(Product.ProductType type);

    boolean existsById(@NotNull String productId);

    @Query("SELECT p FROM Product p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Product> findTitlesByTitleContainingIgnoreCase(@Param("title") String title);

    List<Product> findTop12ByStatusOrderByCreatedAtDesc(Product.Status status);
}