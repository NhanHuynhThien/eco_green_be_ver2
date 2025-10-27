package com.evdealer.evdealermanagement.controller.transactions;

import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.entity.transactions.PurchaseRequest;
import com.evdealer.evdealermanagement.repository.PurchaseRequestRepository;
import com.evdealer.evdealermanagement.service.implement.EmailService;
import com.evdealer.evdealermanagement.utils.VietNamDatetime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    private final EmailService emailService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> payload) {
        log.info("Received Eversign webhook: {}", payload);

        // Lấy document hash từ payload
        String documentHash = (String) payload.get("document_hash");
        String event = (String) payload.get("event");

        log.info("Event: {}, DocumentHash: {}", event, documentHash);

        // Tìm PurchaseRequest theo documentHash
        purchaseRequestRepository.findByContractId(documentHash).ifPresent(request -> {
            switch (event) {
                case "document_signed":
                    // Check signer role
                    List<Map<String, Object>> signers = (List<Map<String, Object>>) payload.get("signers");
                    for (Map<String, Object> signer : signers) {
                        if ((Integer) signer.get("id") == 1 && (Integer) signer.get("signed") == 1) {
                            request.setSellerSignedAt(VietNamDatetime.nowVietNam());
                        }
                        if ((Integer) signer.get("id") == 2 && (Integer) signer.get("signed") == 1) {
                            request.setBuyerSignedAt(VietNamDatetime.nowVietNam());
                        }
                    }
                    break;
                case "document_completed":
                    request.setContractStatus(PurchaseRequest.ContractStatus.COMPLETED);
                    request.setStatus(PurchaseRequest.RequestStatus.COMPLETED);

                    // Update product status
                    request.getProduct().setStatus(Product.Status.SOLD);

                    // Gửi email thông báo
                    emailService.sendContractCompletedNotification(
                            request.getBuyer().getEmail(),
                            request.getSeller().getEmail(),
                            request.getProduct().getTitle());
                    break;
                default:
                    log.info("Unhandled webhook event: {}", event);
            }
            purchaseRequestRepository.save(request);
        });

        return ResponseEntity.ok("Webhook received");
    }
}
