package com.evdealer.evdealermanagement.controller.password;

import com.evdealer.evdealermanagement.dto.password.PasswordResetConfirmDTO;
import com.evdealer.evdealermanagement.dto.password.PasswordResetRequestDTO;
import com.evdealer.evdealermanagement.service.implement.MemberService;
import com.evdealer.evdealermanagement.service.implement.PasswordResetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.evdealer.evdealermanagement.dto.account.password.ChangePasswordRequest;
import com.evdealer.evdealermanagement.dto.account.password.PasswordResponse;
import com.evdealer.evdealermanagement.service.implement.ChangePasswordService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("/password")
@RequiredArgsConstructor
@Slf4j
public class PasswordController {

    private final ChangePasswordService changePasswordService;
    public final PasswordResetService passwordResetService;

    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER', 'STAFF')")
    public PasswordResponse changePassword(@Valid @RequestBody ChangePasswordRequest req,
            Authentication auth) {
        return changePasswordService.changePassword(auth.getName(), req);
    }


}