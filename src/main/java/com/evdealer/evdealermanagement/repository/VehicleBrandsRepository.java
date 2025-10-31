package com.evdealer.evdealermanagement.repository;

import com.evdealer.evdealermanagement.entity.vehicle.VehicleBrands;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleBrandsRepository extends JpaRepository<VehicleBrands, String> {
    boolean existsByNameIgnoreCase(String name);

    Optional<VehicleBrands> findByNameIgnoreCase(String name);

}
