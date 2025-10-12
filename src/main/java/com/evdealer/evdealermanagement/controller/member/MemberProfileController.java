package com.evdealer.evdealermanagement.controller.member;

import com.evdealer.evdealermanagement.dto.account.profile.AccountProfileResponse;
import com.evdealer.evdealermanagement.dto.account.profile.AccountUpdateRequest;
import com.evdealer.evdealermanagement.service.contract.IAccountService;
import com.evdealer.evdealermanagement.service.contract.IUserContextService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class MemberProfileController {

    private final IAccountService accountService;
    private final IUserContextService userContextService;

    @RequestMapping(value = "/me", method = { RequestMethod.PUT, RequestMethod.PATCH })
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<AccountProfileResponse> updateMe(
            @Valid @RequestBody AccountUpdateRequest request) {
        String userId = userContextService.getCurrentUserId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized"));
        return ResponseEntity.ok(accountService.updateProfile(userId, request));
    }
}