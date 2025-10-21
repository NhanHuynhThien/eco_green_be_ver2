package com.evdealer.evdealermanagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evdealer.evdealermanagement.entity.vehicle.VehicleCatalog;

public interface VehicleCatalogRepository extends JpaRepository<VehicleCatalog, String> {
    Optional<VehicleCatalog> findByVersionId(String versionId);

<<<<<<< Updated upstream
    Optional<VehicleCatalog> findByVersion_Id(String versionId);

=======
    Optional<VehicleCatalog> findByVersionIdAndBrandIdAndModelAndYear(
            String versionId,
            String brandId,
            String model,
            Short year);
>>>>>>> Stashed changes
}
