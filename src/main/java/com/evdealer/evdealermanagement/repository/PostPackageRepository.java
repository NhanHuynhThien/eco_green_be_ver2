package com.evdealer.evdealermanagement.repository;

import com.evdealer.evdealermanagement.entity.post.PostPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PostPackageRepository extends JpaRepository<PostPackage, String> {
    List<PostPackage> findByStatusOrderByPriorityLevelDesc(PostPackage.Status status);

    @Query("SELECT p.price FROM PostPackage p WHERE p.code = :code AND p.status = :status")
    BigDecimal getPriceByCode(@Param("code") String code, @Param("status") PostPackage.Status status);
}
