package com.evdealer.evdealermanagement.repository;

import com.evdealer.evdealermanagement.entity.post.PostPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostPaymentRepository extends JpaRepository<PostPayment, String> {
}
