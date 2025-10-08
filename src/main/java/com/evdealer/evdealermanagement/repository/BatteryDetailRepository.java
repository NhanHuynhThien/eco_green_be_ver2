package com.evdealer.evdealermanagement.repository;

import com.evdealer.evdealermanagement.entity.battery.BatteryDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BatteryDetailRepository extends JpaRepository<BatteryDetails, String> {

    // Tìm BatteryDetails theo tên sản phẩm (product.title)
    @Query("SELECT bd FROM BatteryDetails bd " +
           "JOIN bd.product p " +
           "WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<BatteryDetails> findByProductTitleLikeIgnoreCase(@Param("name") String name);

    // Lấy product_id theo tên sản phẩm
    @Query("SELECT p.id FROM BatteryDetails bd JOIN bd.product p " +
           "WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<String> findProductIdsByProductTitle(@Param("name") String name);

    // Lấy product_id theo tên brand (battery brand)
    @Query("SELECT p.id FROM BatteryDetails bd " +
           "JOIN bd.product p " +
           "LEFT JOIN bd.brand b " +
           "WHERE b.name IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :brandName, '%'))")
    List<String> findProductIdsByBrandName(@Param("brandName") String brandName);

    // Tìm theo batteryType name
    @Query("SELECT bd FROM BatteryDetails bd " +
           "JOIN bd.batteryType bt " +
           "WHERE LOWER(bt.name) LIKE LOWER(CONCAT('%', :typeName, '%'))")
    List<BatteryDetails> findByBatteryTypeNameLikeIgnoreCase(@Param("typeName") String typeName);

    // Lọc theo capacity_kwh range
    @Query("SELECT bd FROM BatteryDetails bd " +
           "WHERE bd.capacityKwh BETWEEN :minCap AND :maxCap")
    List<BatteryDetails> findByCapacityKwhBetween(@Param("minCap") BigDecimal minCap,
                                                  @Param("maxCap") BigDecimal maxCap);

    // Lọc theo health_percent range
    @Query("SELECT bd FROM BatteryDetails bd " +
           "WHERE bd.healthPercent BETWEEN :minHp AND :maxHp")
    List<BatteryDetails> findByHealthPercentBetween(@Param("minHp") Integer minHp,
                                                    @Param("maxHp") Integer maxHp);

    // Tìm theo danh sách brand names
    @Query("SELECT bd FROM BatteryDetails bd " +
           "LEFT JOIN bd.brand b " +
           "WHERE b.name IN :brandNames OR b.name IS NULL")
    List<BatteryDetails> findByBrandNameIn(@Param("brandNames") List<String> brandNames);
}