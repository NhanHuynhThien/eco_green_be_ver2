package com.evdealer.evdealermanagement.controller.post;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.evdealer.evdealermanagement.dto.post.PostVerifyRequest;
import com.evdealer.evdealermanagement.dto.post.PostVerifyResponse;
import com.evdealer.evdealermanagement.service.implement.PostVerifyService;

@RestController
@RequestMapping("/staff/products/{productId}/verify")
@RequiredArgsConstructor
public class PostVerifyController {

    private final PostVerifyService postVerifyService;

    @PutMapping
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<PostVerifyResponse> verifyPost(@PathVariable String productId,
            @Valid @RequestBody PostVerifyRequest request) {
        return ResponseEntity.ok(postVerifyService.verifyPost(productId, request));
    }
}