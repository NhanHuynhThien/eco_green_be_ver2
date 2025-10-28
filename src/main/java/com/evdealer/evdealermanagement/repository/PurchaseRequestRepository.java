package com.evdealer.evdealermanagement.repository;

import com.evdealer.evdealermanagement.entity.transactions.PurchaseRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, String> {

    List<PurchaseRequest> findByProductId(String productId);

    Page<PurchaseRequest> findByBuyerId(String buyerId, Pageable pageable);

    Page<PurchaseRequest> findBySellerId(String sellerId, Pageable pageable);

    Page<PurchaseRequest> findByBuyerIdAndStatus(
            String buyerId,
            PurchaseRequest.RequestStatus status,
            Pageable pageable);

    Page<PurchaseRequest> findBySellerIdAndStatus(
            String sellerId,
            PurchaseRequest.RequestStatus status,
            Pageable pageable);

    @Query("SELECT pr FROM PurchaseRequest pr " +
            "WHERE pr.product.id = :productId " +
            "AND pr.buyer.id = :buyerId " +
            "AND pr.status NOT IN ('REJECTED', 'CANCELLED', 'EXPIRED')")
    Optional<PurchaseRequest> findActivePurchaseRequest(
            @Param("productId") String productId,
            @Param("buyerId") String buyerId);

    long countBySellerIdAndStatus(String sellerId, PurchaseRequest.RequestStatus status);

    @Query("SELECT pr FROM PurchaseRequest pr " +
            "WHERE pr.contractId = :contractId")
    Optional<PurchaseRequest> findByContractId(@Param("contractId") String contractId);
}
