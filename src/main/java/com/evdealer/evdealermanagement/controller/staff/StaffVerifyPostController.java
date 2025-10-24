package com.evdealer.evdealermanagement.controller.staff;

import com.evdealer.evdealermanagement.dto.post.verification.PostVerifyRequest;
import com.evdealer.evdealermanagement.dto.post.verification.PostVerifyResponse;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.entity.report.Report;
import com.evdealer.evdealermanagement.service.implement.ReportService;
import com.evdealer.evdealermanagement.service.implement.StaffService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/staff/post")
@RequiredArgsConstructor
public class StaffVerifyPostController {

    private final StaffService staffService;
    private final ReportService reportService;

    @PostMapping("/{productId}/verify/active")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<PostVerifyResponse> approvePost(@PathVariable String productId) {
        PostVerifyResponse response = staffService.verifyPostActive(productId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{productId}/verify/reject")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<PostVerifyResponse> rejectPost(
            @PathVariable String productId,
            @Valid @RequestBody PostVerifyRequest request) {
        PostVerifyResponse response = staffService.verifyPostReject(productId, request.getRejectReason());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending/review")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<Page<PostVerifyResponse>> getPendingPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PostVerifyResponse> pendingPosts = staffService.getListVerifyPost(pageable);
        return ResponseEntity.ok(pendingPosts);
    }

    @GetMapping("/pending/review/type")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<Page<PostVerifyResponse>> getPendingPostsByType(
            @RequestParam(name = "type", required = false) Product.ProductType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PostVerifyResponse> pendingPosts = staffService.getListVerifyPostByType(type, pageable);
        return ResponseEntity.ok(pendingPosts);
    }

    @PatchMapping("/reports/{reportId}/status")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<Report.ReportStatus> resolveReport(@PathVariable String reportId) {
        Report.ReportStatus status = reportService.updateStatusReport(reportId);
        return ResponseEntity.ok(status);
    }

}
