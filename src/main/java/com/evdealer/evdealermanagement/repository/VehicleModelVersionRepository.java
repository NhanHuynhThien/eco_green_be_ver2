package com.evdealer.evdealermanagement.repository;

import com.evdealer.evdealermanagement.entity.vehicle.ModelVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleModelVersionRepository extends JpaRepository<ModelVersion, String> {

    List<ModelVersion> findAllByModel_Id(String modelId);
}
