package com.evdealer.evdealermanagement.controller.member;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evdealer.evdealermanagement.service.implement.ProfileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/member/profile/me")
@RequiredArgsConstructor
public class MemberAccountController {

    private final ProfileService profileService;

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<Void> deleteUser(Authentication authentication) {
        profileService.deleteAccount(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
