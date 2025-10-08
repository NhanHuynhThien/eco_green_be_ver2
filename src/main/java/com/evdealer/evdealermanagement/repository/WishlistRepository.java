package com.evdealer.evdealermanagement.repository;

import com.evdealer.evdealermanagement.entity.wishlist.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, String> {
    boolean existsByAccountId(String accountId);

    Optional<Wishlist> findByAccountId(String accountId);
}