package com.evdealer.evdealermanagement.controller.transactions;

import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.entity.transactions.PurchaseRequest;
import com.evdealer.evdealermanagement.repository.ProductRepository;
import com.evdealer.evdealermanagement.repository.PurchaseRequestRepository;
import com.evdealer.evdealermanagement.service.implement.EmailService;
import com.evdealer.evdealermanagement.utils.VietNamDatetime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("member/eversign")
@Slf4j
@RequiredArgsConstructor
public class EversignWebhookController {

    private final PurchaseRequestRepository purchaseRequestRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;

    @PostMapping("/webhook")
    @Transactional
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> payload) {
        try {
            log.info("Received Eversign webhook: {}", payload);

            String documentHash = (String) payload.get("document_hash");
            String event = (String) payload.get("event");

            if (documentHash == null || event == null) {
                log.error("Missing document_hash or event in webhook payload");
                return ResponseEntity.badRequest().body("Missing required fields");
            }

            log.info("Event: {}, DocumentHash: {}", event, documentHash);

            PurchaseRequest request = purchaseRequestRepository.findByContractId(documentHash)
                    .orElseThrow(() -> {
                        log.error("❌ PurchaseRequest not found for contractId: {}", documentHash);
                        return new RuntimeException("Request not found");
                    });

            switch (event) {
                case "document_signed":
                    handleDocumentSigned(request, payload);
                    break;

                case "document_completed":
                    handleDocumentCompleted(request);
                    break;

                default:
                    log.info("Unhandled webhook event: {}", event);
            }

            purchaseRequestRepository.save(request);
            log.info("Webhook processed successfully for request: {}", request.getId());

            return ResponseEntity.ok("Webhook received");

        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing webhook: " + e.getMessage());
        }
    }

    private void handleDocumentSigned(PurchaseRequest request, Map<String, Object> payload) {
        try {
            List<Map<String, Object>> signers = (List<Map<String, Object>>) payload.get("signers");

            if (signers == null || signers.isEmpty()) {
                log.warn("No signers found in webhook payload");
                return;
            }

            for (Map<String, Object> signer : signers) {
                Integer signerId = getIntegerValue(signer.get("id"));
                Integer signed = getIntegerValue(signer.get("signed"));

                if (signerId == null || signed == null) {
                    continue;
                }

                // ID 1 = Seller, ID 2 = Buyer (theo thứ tự tạo contract)
                if (signerId == 1 && signed == 1 && request.getSellerSignedAt() == null) {
                    request.setSellerSignedAt(VietNamDatetime.nowVietNam());
                    updateContractStatus(request, "seller");
                    log.info("Seller signed contract. Request ID: {}", request.getId());
                }

                if (signerId == 2 && signed == 1 && request.getBuyerSignedAt() == null) {
                    request.setBuyerSignedAt(VietNamDatetime.nowVietNam());
                    updateContractStatus(request, "buyer");
                    log.info("Buyer signed contract. Request ID: {}", request.getId());
                }
            }

            // Auto-complete nếu cả 2 đã ký
            if (request.getBuyerSignedAt() != null && request.getSellerSignedAt() != null) {
                handleDocumentCompleted(request);
            }

        } catch (Exception e) {
            log.error("Error handling document_signed: {}", e.getMessage(), e);
        }
    }

    private void updateContractStatus(PurchaseRequest request, String signerRole) {
        if ("seller".equals(signerRole)) {
            if (request.getContractStatus() == PurchaseRequest.ContractStatus.BUYER_SIGNED) {
                request.setContractStatus(PurchaseRequest.ContractStatus.COMPLETED);
            } else {
                request.setContractStatus(PurchaseRequest.ContractStatus.SELLER_SIGNED);
            }
        } else if ("buyer".equals(signerRole)) {
            if (request.getContractStatus() == PurchaseRequest.ContractStatus.SELLER_SIGNED) {
                request.setContractStatus(PurchaseRequest.ContractStatus.COMPLETED);
            } else {
                request.setContractStatus(PurchaseRequest.ContractStatus.BUYER_SIGNED);
            }
        }
    }

    private void handleDocumentCompleted(PurchaseRequest request) {
        try {
            request.setContractStatus(PurchaseRequest.ContractStatus.COMPLETED);
            request.setStatus(PurchaseRequest.RequestStatus.COMPLETED);

            // Update product status
            Product product = request.getProduct();
            product.setStatus(Product.Status.SOLD);
            productRepository.save(product);

            log.info("Contract completed. Product {} marked as SOLD", product.getId());

            // Send email notification
            try {
                emailService.sendContractCompletedNotification(
                        request.getBuyer().getEmail(),
                        request.getSeller().getEmail(),
                        product.getTitle());
                log.info("Completion email sent successfully");
            } catch (Exception e) {
                log.error("Failed to send completion email: {}", e.getMessage());
            }

        } catch (Exception e) {
            log.error("Error handling document_completed: {}", e.getMessage(), e);
        }
    }

    private Integer getIntegerValue(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}