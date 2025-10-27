package com.evdealer.evdealermanagement.controller.transactions;

import com.evdealer.evdealermanagement.dto.transactions.CreatePurchaseRequestDTO;
import com.evdealer.evdealermanagement.dto.transactions.PurchaseRequestResponse;
import com.evdealer.evdealermanagement.service.implement.PurchaseRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("member/purchase-request")
@RequiredArgsConstructor
@Slf4j
public class PurchaseRequestController {

    private final PurchaseRequestService purchaseRequestService;

    @PostMapping("/create")
    public ResponseEntity<PurchaseRequestResponse> create(@RequestBody CreatePurchaseRequestDTO dto) {
        PurchaseRequestResponse response = purchaseRequestService.createPurchaseRequest(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<PurchaseRequestResponse> accept(@PathVariable String id) {
        PurchaseRequestResponse response = purchaseRequestService.acceptPurchaseRequest(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<PurchaseRequestResponse> reject(
            @PathVariable String id,
            @RequestParam String reason) {
        PurchaseRequestResponse response = purchaseRequestService.rejectPurchaseRequest(id, reason);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<PurchaseRequestResponse> complete(
            @PathVariable String id,
            @RequestParam boolean buyerSigned,
            @RequestParam boolean sellerSigned) {
        PurchaseRequestResponse response = purchaseRequestService.completeContract(id, buyerSigned, sellerSigned);
        return ResponseEntity.ok(response);
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<PurchaseRequestResponse> get(@PathVariable String id) {
//        PurchaseRequestResponse response = purchaseRequestService.;
//        return ResponseEntity.ok(response);
//    }
}
