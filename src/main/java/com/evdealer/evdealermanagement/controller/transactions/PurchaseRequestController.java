package com.evdealer.evdealermanagement.controller.transactions;

import com.evdealer.evdealermanagement.dto.transactions.CreatePurchaseRequestDTO;
import com.evdealer.evdealermanagement.dto.transactions.PurchaseRequestResponse;
import com.evdealer.evdealermanagement.dto.transactions.SellerResponseDTO;
import com.evdealer.evdealermanagement.service.implement.PurchaseRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("member/purchase-request")
@RequiredArgsConstructor
@Slf4j
public class PurchaseRequestController {

    private final PurchaseRequestService purchaseRequestService;

    @PostMapping("/create")
    public ResponseEntity<PurchaseRequestResponse> create(@Valid @RequestBody CreatePurchaseRequestDTO dto) {
        log.info("Creating purchase request for product: {}", dto.getProductId());
        PurchaseRequestResponse response = purchaseRequestService.createPurchaseRequest(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/respond")
    public ResponseEntity<PurchaseRequestResponse> respond(@Valid @RequestBody SellerResponseDTO dto) {
        log.info("Seller responding to purchase request: {}", dto.getRequestId());
        PurchaseRequestResponse response = purchaseRequestService.respondToPurchaseRequest(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/buyer")
    public ResponseEntity<Page<PurchaseRequestResponse>> getBuyerRequests(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Fetching buyer's purchase requests");
        Page<PurchaseRequestResponse> requests = purchaseRequestService.getBuyerRequests(pageable);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/seller")
    public ResponseEntity<Page<PurchaseRequestResponse>> getSellerRequests(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
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