package com.evdealer.evdealermanagement.repository;

import com.evdealer.evdealermanagement.entity.vehicle.VehicleCategories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleCategoryRepository extends JpaRepository<VehicleCategories, String> {
    Optional<VehicleCategories> findById(String id);
}
