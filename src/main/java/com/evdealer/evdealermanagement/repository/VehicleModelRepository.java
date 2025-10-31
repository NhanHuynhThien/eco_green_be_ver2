package com.evdealer.evdealermanagement.repository;

import com.evdealer.evdealermanagement.entity.vehicle.Model;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleModelRepository extends JpaRepository<Model, String> {

    List<Model> findAllByBrand_IdAndVehicleType_Id(String brandId, String vehicleTypeId);

    Model findByName(String productName);

    Optional<Model> findByNameIgnoreCaseAndBrandIdAndVehicleTypeId(String name, String brandId, String vehicleTypeId);
}
