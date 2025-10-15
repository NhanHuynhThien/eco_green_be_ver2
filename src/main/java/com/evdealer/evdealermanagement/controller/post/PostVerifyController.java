package com.evdealer.evdealermanagement.controller.post;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.evdealer.evdealermanagement.dto.post.verification.PostVerifyRequest;
import com.evdealer.evdealermanagement.dto.post.verification.PostVerifyResponse;
import com.evdealer.evdealermanagement.service.implement.PostVerifyService;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostVerifyController {

    private final PostVerifyService postVerifyService;

    @PutMapping("/{productId}/verify")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<PostVerifyResponse> verifyPost(@PathVariable String productId,
            @Valid @RequestBody PostVerifyRequest request) {
        return ResponseEntity.ok(postVerifyService.verifyPost(productId, request));
    }
}