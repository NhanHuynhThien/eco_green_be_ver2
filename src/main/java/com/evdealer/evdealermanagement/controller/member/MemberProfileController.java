package com.evdealer.evdealermanagement.controller.member;

import com.evdealer.evdealermanagement.dto.account.profile.AccountProfileResponse;
import com.evdealer.evdealermanagement.dto.account.profile.AccountUpdateRequest;
import com.evdealer.evdealermanagement.service.contract.IAccountService;
<<<<<<< HEAD
import com.evdealer.evdealermanagement.service.contract.IUserContextService;

=======
import com.evdealer.evdealermanagement.service.implement.ProfileService;
>>>>>>> 1cf309f66c3668b84b4b0f70c5b3d3bf6e470289
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class MemberProfileController {

    private final IAccountService accountService;
    private final IUserContextService userContextService;

<<<<<<< HEAD
    @RequestMapping(value = "/me", method = { RequestMethod.PUT, RequestMethod.PATCH })
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<AccountProfileResponse> updateMe(
            @Valid @RequestBody AccountUpdateRequest request) {
        String userId = userContextService.getCurrentUserId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized"));
        return ResponseEntity.ok(accountService.updateProfile(userId, request));
=======
    private final ProfileService profileService;

    @PatchMapping
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<AccountProfileResponse> updateProfile(
            @Valid @RequestBody AccountUpdateRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(profileService.updateProfile(userId, request));
>>>>>>> 1cf309f66c3668b84b4b0f70c5b3d3bf6e470289
    }

}