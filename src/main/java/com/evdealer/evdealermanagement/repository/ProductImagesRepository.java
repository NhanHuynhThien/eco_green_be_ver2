package com.evdealer.evdealermanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evdealer.evdealermanagement.entity.product.ProductImages;

public interface ProductImagesRepository extends JpaRepository<ProductImages, String> {
    List<ProductImages> findByProductIdOrderByPositionAsc(String productId);
}
