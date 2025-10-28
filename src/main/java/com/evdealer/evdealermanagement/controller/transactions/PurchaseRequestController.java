package com.evdealer.evdealermanagement.controller.transactions;

import com.evdealer.evdealermanagement.dto.transactions.CreatePurchaseRequestDTO;
import com.evdealer.evdealermanagement.dto.transactions.PurchaseRequestResponse;
import com.evdealer.evdealermanagement.dto.transactions.SellerResponseDTO;
import com.evdealer.evdealermanagement.service.implement.PurchaseRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/member/purchase-request")
@RequiredArgsConstructor
@Slf4j
public class PurchaseRequestController {

    private final PurchaseRequestService purchaseRequestService;

    // ĐẶT URL BASE CỦA FRONTEND CỦA BẠN TẠI ĐÂY
    private static final String FRONTEND_BASE_URL = "https://evdealer.com";

    // =======================================================
    // 1. API Gửi Yêu Cầu Mua Hàng
    // =======================================================

    @PostMapping("/create")
    public ResponseEntity<PurchaseRequestResponse> create(@Valid @RequestBody CreatePurchaseRequestDTO dto) {
        log.info("Creating purchase request for product: {}", dto.getProductId());
        PurchaseRequestResponse response = purchaseRequestService.createPurchaseRequest(dto);
        return ResponseEntity.ok(response);
    }

    // =======================================================
    // 2. API Phản Hồi Yêu Cầu (Dành cho Frontend/Nội bộ)
    // =======================================================

    @PostMapping("/respond")
    public ResponseEntity<PurchaseRequestResponse> respond(@Valid @RequestBody SellerResponseDTO dto) {
        log.info("Seller responding to purchase request via POST: {}", dto.getRequestId());
        PurchaseRequestResponse response = purchaseRequestService.respondToPurchaseRequest(dto);
        return ResponseEntity.ok(response);
    }

    // =======================================================
    // 3. API Phản Hồi Yêu Cầu (Dành cho Liên kết Email - Cần Đăng nhập)
    // =======================================================

    /**
     * Endpoint xử lý phản hồi từ liên kết Email.
     * LƯU Ý: Vẫn yêu cầu người dùng phải ĐÃ ĐĂNG NHẬP do gọi Service sử dụng UserContextService.
     *
     * @param requestId ID của yêu cầu mua hàng.
     * @param accept Quyết định của người bán (true/false).
     * @return Chuyển hướng (HTTP 302) đến trang xác nhận trên frontend.
     */
    @GetMapping("/respond/email")
    public ResponseEntity<?> respondFromEmail(
            @RequestParam String requestId,
            @RequestParam boolean accept) {

        log.info("Seller responding from email link. Request ID: {}, Accept: {}", requestId, accept);

        // 1. Chuẩn bị DTO từ Query Parameters
        SellerResponseDTO dto = new SellerResponseDTO();
        dto.setRequestId(requestId);
        dto.setAccept(accept);

        // Thiết lập message mặc định
        String defaultMessage = accept
                ? "Đồng ý bán sản phẩm. Vui lòng xem và ký hợp đồng."
                : "Xin lỗi, hiện tại tôi chưa thể bán sản phẩm này.";
        dto.setResponseMessage(defaultMessage);
        dto.setRejectReason(defaultMessage);


        try {
            // 2. Gọi Service xử lý logic phản hồi
            PurchaseRequestResponse response = purchaseRequestService.respondToPurchaseRequest(dto);

            // 3. Xây dựng URL chuyển hướng đến Frontend
            // Chuyển hướng đến trang chi tiết hoặc trang kết quả
            String redirectUrl = String.format(FRONTEND_BASE_URL + "/seller/requests/%s?status=%s",
                    requestId, response.getStatus());

            log.info("Request processed successfully. Redirecting to: {}", redirectUrl);

            // 4. Trả về phản hồi chuyển hướng (HTTP 302 Found)
            return ResponseEntity.status(302)
                    .location(URI.create(redirectUrl))
                    .build();

        } catch (ResponseStatusException e) {
            // Bắt lỗi Service (ví dụ: UNAUTHORIZED, BAD_REQUEST, INTERNAL_SERVER_ERROR do Eversign)
            log.error("Error responding to purchase request {} from email: {}", requestId, e.getReason(), e);

            // Chuyển hướng đến trang lỗi nếu có vấn đề
            String errorRedirectUrl = String.format(FRONTEND_BASE_URL + "/error?message=purchase_request_error&reason=%s",
                    e.getReason().replace(' ', '+')); // Thay thế khoảng trắng để truyền qua URL

            return ResponseEntity.status(302)
                    .location(URI.create(errorRedirectUrl))
                    .build();
        } catch (Exception e) {
            // Bắt lỗi không xác định
            log.error("Unexpected error processing email response for request {}: {}", requestId, e.getMessage(), e);

            String errorRedirectUrl = String.format(FRONTEND_BASE_URL + "/error?message=purchase_request_internal_error");

            return ResponseEntity.status(302)
                    .location(URI.create(errorRedirectUrl))
                    .build();
        }
    }

    // =======================================================
    // 4. API Truy Vấn Thông Tin (Giữ nguyên)
    // =======================================================

    @GetMapping("/buyer")
    public ResponseEntity<Page<PurchaseRequestResponse>> getBuyerRequests(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        log.info("Fetching buyer's purchase requests");
        Page<PurchaseRequestResponse> requests = purchaseRequestService.getBuyerRequests(pageable);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/seller")
    public ResponseEntity<Page<PurchaseRequestResponse>> getSellerRequests(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        log.info("Fetching seller's purchase requests");
        Page<PurchaseRequestResponse> requests = purchaseRequestService.getSellerRequests(pageable);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/seller/pending-count")
    public ResponseEntity<Long> getPendingCount() {
        log.info("Fetching pending seller requests count");
        long count = purchaseRequestService.countPendingSellerRequests();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseRequestResponse> getDetail(@PathVariable String id) {
        log.info("Fetching purchase request detail: {}", id);
        PurchaseRequestResponse response = purchaseRequestService.getRequestDetail(id);
        return ResponseEntity.ok(response);
    }
}