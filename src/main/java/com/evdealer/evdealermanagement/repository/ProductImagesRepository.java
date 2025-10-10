package com.evdealer.evdealermanagement.repository;

import com.evdealer.evdealermanagement.entity.product.ProductImages;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImagesRepository extends JpaRepository<ProductImages, Integer> {
    List<ProductImages> findByProductIdOrderByPositionAsc(String productId);
}
