package com.evdealer.evdealermanagement.repository;

import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.entity.vehicle.VehicleDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleDetailsRepository extends JpaRepository<VehicleDetails, String> {

        // Tìm VehicleDetails theo tên sản phẩm
        @Query("SELECT vd FROM VehicleDetails vd " +
                        "JOIN vd.product p " +
                        "WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :name, '%'))")
        List<VehicleDetails> findVehicleDetailsByProductName(@Param("name") String name);

        // Lấy Product ID theo tên sản phẩm
        @Query("SELECT vd.product.id FROM VehicleDetails vd " +
                        "JOIN vd.product p " +
                        "WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :name, '%'))")
        List<Long> findVehicleProductIdsByName(@Param("name") String name);

        // Tìm theo tên hãng xe
        @Query("SELECT vd.product.id FROM VehicleDetails vd " +
                        "JOIN vd.brand vb " +
                        "WHERE LOWER(vb.name) LIKE LOWER(CONCAT('%', :brandName, '%'))")
        List<Long> findVehicleProductIdsByBrand(@Param("brandName") String brandName);

        // Tìm xe theo model
        @Query("SELECT vd FROM VehicleDetails vd " +
                        "WHERE LOWER(vd.model) LIKE LOWER(CONCAT('%', :model, '%'))")
        List<VehicleDetails> findVehiclesByModel(@Param("model") String model);

        // Tìm xe theo năm sản xuất
        @Query("SELECT vc FROM VehicleCatalog vc " +
                        "WHERE vc.year = :year")
        List<VehicleDetails> findVehiclesByYear(@Param("year") Integer year);

        // Tìm xe theo category
        @Query("SELECT vd FROM VehicleDetails vd " +
                        "JOIN vd.category vc " +
                        "WHERE LOWER(vc.name) LIKE LOWER(CONCAT('%', :categoryName, '%'))")
        List<VehicleDetails> findVehiclesByCategory(@Param("categoryName") String categoryName);

        // Tìm xe theo price range
        @Query("SELECT vd FROM VehicleDetails vd " +
                        "JOIN vd.product p " +
                        "WHERE p.price BETWEEN :minPrice AND :maxPrice " +
                        "AND p.status = 'active'")
        List<VehicleDetails> findVehiclesByPriceRange(@Param("minPrice") Double minPrice,
                        @Param("maxPrice") Double maxPrice);

        // Tìm xe theo tốc độ tối thiểu
        @Query("SELECT vc FROM VehicleCatalog vc " +
                        "WHERE vc.topSpeedKmh >= :minSpeed")
        List<VehicleDetails> findVehiclesByMinSpeed(@Param("minSpeed") Integer minSpeed);

        // Tìm xe theo range tối thiểu
        @Query("SELECT vc FROM VehicleCatalog vc " +
                        "WHERE vc.rangeKm >= :minRange")
        List<VehicleDetails> findVehiclesByMinRange(@Param("minRange") Integer minRange);

        // Tìm xe có pin tháo rời
        @Query("SELECT vc FROM VehicleCatalog vc " +
                        "WHERE vc.removableBattery = true")
        List<VehicleDetails> findVehiclesWithRemovableBattery();

        // Tìm xe theo tình trạng sức khỏe pin
        @Query("SELECT vd FROM VehicleDetails vd " +
                        "WHERE vd.batteryHealthPercent >= :minHealth")
        List<VehicleDetails> findVehiclesByBatteryHealth(@Param("minHealth") Integer minHealth);

        // Lấy xe theo danh sách hãng
        @Query("SELECT vd FROM VehicleDetails vd " +
                        "JOIN vd.brand vb " +
                        "WHERE vb.name IN :brandNames")
        List<VehicleDetails> findByBrandNames(@Param("brandNames") List<String> brandNames);

        // Tìm VehicleDetails theo Product ID
        @Query("SELECT vd FROM VehicleDetails vd " +
                        "WHERE vd.product.id = :productId")
        Optional<VehicleDetails> findByProductId(@Param("productId") String productId);

        // Tìm xe tương tự theo model
        @Query("SELECT vd.product FROM VehicleDetails vd " +
                "WHERE vd.model.id = :modelId " +
                "AND vd.product.id <> :productId " +
                "AND vd.product.status = 'ACTIVE'" +
                "ORDER BY vd.product.createdAt DESC")
        List<Product> findSimilarVehiclesByModel(@Param("modelId") String modelId,
                                                 @Param("productId") String productId);

        // Tìm xe tương tự theo brand
        @Query("SELECT vd.product FROM VehicleDetails vd " +
                "WHERE vd.brand.id = :brandId " +
                "AND vd.model.id <> :modelId " +
                "AND vd.product.id <> :productId " +
                "AND vd.product.status = 'ACTIVE'" +
                "ORDER BY vd.product.createdAt DESC")
        List<Product> findSimilarVehiclesByBrand(@Param("brandId") String brandId,
                                                 @Param("modelId") String modelId,
                                                 @Param("productId") String productId);
}
