package com.evdealer.evdealermanagement.controller.transactions;

import com.evdealer.evdealermanagement.entity.transactions.PurchaseRequest;
import com.evdealer.evdealermanagement.repository.PurchaseRequestRepository;
import com.evdealer.evdealermanagement.service.implement.EversignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks/eversign")
@RequiredArgsConstructor
@Slf4j
public class EversignWebhookController {

    private final PurchaseRequestRepository purchaseRequestRepository;
    private final EversignService eversignService;

    /**
     * Webhook cho document completed (all signers signed)
     */
    @PostMapping("/document-complete")
    public ResponseEntity<?> handleDocumentComplete(@RequestBody(required = false) Map<String, Object> payload) {
        // Thêm (required = false) để tránh lỗi khi truy cập thủ công
        try {
            // Kiểm tra payload và document_hash để xử lý lỗi 500
            if (payload == null || !payload.containsKey("document_hash")) {
                log.error("❌ Webhook nhận được body rỗng hoặc thiếu 'document_hash'");
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Invalid payload"));
            }

            log.info("🎉 Webhook Document Completed được nhận: {}", payload);
            String documentHash = (String) payload.get("document_hash");

            // Giao toàn bộ việc xử lý cho Service trong một transaction duy nhất
            eversignService.processDocumentCompletion(documentHash);

            return ResponseEntity.ok(Map.of("success", true));

        } catch (Exception e) {
            log.error("❌ Lỗi nghiêm trọng khi xử lý webhook: {}", e.getMessage(), e);
            // Trả về lỗi 500 nếu có bất kỳ lỗi nào xảy ra trong service
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}