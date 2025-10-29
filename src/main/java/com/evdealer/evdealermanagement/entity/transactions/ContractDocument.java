package com.evdealer.evdealermanagement.entity.transactions;

import com.evdealer.evdealermanagement.utils.VietNamDatetime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "contract_documents")
@Getter
@Setter
public class ContractDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String documentId;
    private String title;
    private String pdfUrl;
    private String signerEmail;
    private LocalDateTime signedAt;

    @ManyToOne
    @JoinColumn(name = "purchase_request_id")
    private PurchaseRequest purchaseRequest;
}
