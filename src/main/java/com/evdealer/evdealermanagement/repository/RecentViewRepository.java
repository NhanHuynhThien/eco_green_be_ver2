package com.evdealer.evdealermanagement.repository;

import com.evdealer.evdealermanagement.dto.product.detail.ProductDetail;
import com.evdealer.evdealermanagement.entity.account.RecentView;
import com.evdealer.evdealermanagement.entity.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecentViewRepository extends JpaRepository<RecentView, Long> {

    void deleteByUserIdAndProductId(String userid, String productId);

    List<RecentView> findByUserIdOrderByViewedAtDesc(String userId);

    @Query("""
                SELECT rv.product FROM RecentView rv
                LEFT JOIN FETCH rv.product.images
                WHERE rv.user.id = :userId
                ORDER BY rv.viewedAt DESC
            """)
    List<Product> findProductsByUserId(@Param("userId") String userId);

}
