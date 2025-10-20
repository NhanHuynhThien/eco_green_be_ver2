package com.evdealer.evdealermanagement.controller.staff;

import com.evdealer.evdealermanagement.dto.post.verification.PostVerifyRequest;
import com.evdealer.evdealermanagement.dto.post.verification.PostVerifyResponse;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.service.implement.StaffService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/staff/post")
@RequiredArgsConstructor
public class StaffVerifyPostController {

    private final StaffService staffService;

    @PutMapping("/{productId}/verify")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<PostVerifyResponse> verifyPost(
            @PathVariable String productId,
            @Valid @RequestBody PostVerifyRequest request) {

        PostVerifyResponse response = staffService.verifyPost(productId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending/review")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<List<PostVerifyResponse>> getPendingPosts() {
        List<PostVerifyResponse> pendingPosts = staffService.getListVerifyPost();
        return ResponseEntity.ok(pendingPosts);
    }

    @GetMapping("/pending/review/type")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<List<PostVerifyResponse>> getPendingPostsByType(
            @RequestParam(name = "type", required = false) Product.ProductType type) {
        List<PostVerifyResponse> pendingPosts = staffService.getListVerifyPostByType(type);
        return ResponseEntity.ok(pendingPosts);
    }

}
