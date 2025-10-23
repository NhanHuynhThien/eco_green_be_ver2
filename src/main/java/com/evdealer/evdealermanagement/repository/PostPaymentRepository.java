package com.evdealer.evdealermanagement.repository;

import com.evdealer.evdealermanagement.entity.post.PostPayment;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostPaymentRepository extends JpaRepository<PostPayment, String> {
    Optional<PostPayment> findTopByProductIdAndPaymentStatusOrderByIdDesc(
            String productId,
            PostPayment.PaymentStatus status);

    boolean existsByAccountIdAndPaymentStatus(String accountId, PostPayment.PaymentStatus status);

    Optional<PostPayment> findTopByProductIdAndPaymentStatusOrderByCreatedAtDesc(
            String productId,
            PostPayment.PaymentStatus paymentStatus);
}
