package com.evdealer.evdealermanagement.controller.member;

import com.evdealer.evdealermanagement.dto.account.profile.AccountProfileResponse;
import com.evdealer.evdealermanagement.dto.account.profile.AccountUpdateRequest;
import com.evdealer.evdealermanagement.service.implement.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class MemberProfileController {

    private final ProfileService profileService;

    @PutMapping
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<AccountProfileResponse> updateProfile(
            @Valid @RequestBody AccountUpdateRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(profileService.updateProfile(userId, request));
    }

}