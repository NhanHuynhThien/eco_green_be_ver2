package com.evdealer.evdealermanagement.controller.transactions;

import com.evdealer.evdealermanagement.entity.transactions.PurchaseRequest;
import com.evdealer.evdealermanagement.repository.PurchaseRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks/eversign")
@RequiredArgsConstructor
@Slf4j
public class EversignWebhookController {

    private final PurchaseRequestRepository purchaseRequestRepository;

    /**
     * Webhook endpoint để Eversign gọi về khi có sự kiện
     * Config webhook URL trong Eversign Dashboard:
     * https://yourdomain.com/api/webhooks/eversign/signature-complete
     */
    @PostMapping("/signature-complete")
    public ResponseEntity<?> handleSignatureComplete(@RequestBody Map<String, Object> payload) {
        try {
            log.info("🔔 Received Eversign webhook: {}", payload);

            // Parse webhook data
            String eventType = (String) payload.get("event_type");
            Map<String, Object> eventData = (Map<String, Object>) payload.get("event_hash");

            if (!"document_signed".equals(eventType)) {
                log.info("⚠️ Ignoring event type: {}", eventType);
                return ResponseEntity.ok("Event ignored");
            }

            String documentHash = (String) eventData.get("document_hash");
            Map<String, Object> signerData = (Map<String, Object>) eventData.get("signer");
            String signerEmail = (String) signerData.get("email");

            log.info("📝 Document signed: {} by {}", documentHash, signerEmail);

            // Tìm purchase request theo contractId
            PurchaseRequest request = purchaseRequestRepository
                    .findByContractId(documentHash)
                    .orElse(null);

            if (request == null) {
                log.warn("⚠️ No purchase request found for contract: {}", documentHash);
                return ResponseEntity.ok("No matching request");
            }

            // Update signing status
            if (signerEmail.equalsIgnoreCase(request.getBuyer().getEmail())) {
                request.setBuyerSignedAt(LocalDateTime.now());
                log.info("✅ Buyer signed: {}", signerEmail);
            } else if (signerEmail.equalsIgnoreCase(request.getSeller().getEmail())) {
                request.setSellerSignedAt(LocalDateTime.now());
                log.info("✅ Seller signed: {}", signerEmail);
            }

            // Check if both signed
            if (request.getBuyerSignedAt() != null && request.getSellerSignedAt() != null) {
                request.setContractStatus(PurchaseRequest.ContractStatus.COMPLETED);
                request.setStatus(PurchaseRequest.RequestStatus.CONTRACT_SIGNED);
                log.info("🎉 CONTRACT FULLY SIGNED! Request: {}", request.getId());

                // TODO: Trigger next steps (payment, delivery, etc.)
            }

            purchaseRequestRepository.save(request);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Signature recorded"
            ));

        } catch (Exception e) {
            log.error("❌ Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Webhook cho document completed (all signers signed)
     */
    @PostMapping("/document-complete")
    public ResponseEntity<?> handleDocumentComplete(@RequestBody Map<String, Object> payload) {
        try {
            log.info("🎉 Document completed webhook: {}", payload);

            String documentHash = (String) payload.get("document_hash");

            PurchaseRequest request = purchaseRequestRepository
                    .findByContractId(documentHash)
                    .orElse(null);

            if (request != null) {
                request.setContractStatus(PurchaseRequest.ContractStatus.COMPLETED);
                request.setStatus(PurchaseRequest.RequestStatus.CONTRACT_SIGNED);
                purchaseRequestRepository.save(request);

                log.info("✅ Contract marked as completed: {}", request.getId());
            }

            return ResponseEntity.ok(Map.of("success", true));

        } catch (Exception e) {
            log.error("❌ Error processing completion webhook: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("success", false));
        }
    }
}