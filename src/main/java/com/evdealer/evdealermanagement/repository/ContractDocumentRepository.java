package com.evdealer.evdealermanagement.repository;

import com.evdealer.evdealermanagement.entity.transactions.ContractDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContractDocumentRepository extends JpaRepository<ContractDocument, Long> {
    Optional<ContractDocument> findByDocumentId(String documentId);
}