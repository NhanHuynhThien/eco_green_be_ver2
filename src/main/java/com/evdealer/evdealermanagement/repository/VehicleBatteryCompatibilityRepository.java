package com.evdealer.evdealermanagement.repository;

import com.evdealer.evdealermanagement.entity.compatibility.VehicleBatteryCompatibility;
import com.evdealer.evdealermanagement.entity.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleBatteryCompatibilityRepository extends JpaRepository<VehicleBatteryCompatibility, Long> {

    List<VehicleBatteryCompatibility> findByVehicle(Product vehicle);
    List<VehicleBatteryCompatibility> findByBattery(Product battery);
}
