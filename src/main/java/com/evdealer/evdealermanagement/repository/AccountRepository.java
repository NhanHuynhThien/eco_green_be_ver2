package com.evdealer.evdealermanagement.repository;

import com.evdealer.evdealermanagement.entity.account.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByUsername(String username);

    Optional<Account> findByPhone(String phone);

    boolean existsByUsernameAndIdNot(String username, String id); // Changed Long to String

    boolean existsByPhoneAndIdNot(String phone, String id); // Changed Long to String

    Optional<Account> findByEmail(String email);

    boolean existsByEmailAndIdNot(String email, String id);

    @Query("SELECT a.username FROM Account a WHERE a.phone = :phone")
    String findUsernameByPhone(String phone);

    @EntityGraph(attributePaths = { "role" }) // nếu có quan hệ role
    Optional<Account> findWithDetailsByEmail(String email);
}