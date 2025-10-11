package com.evdealer.evdealermanagement.controller.auth;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evdealer.evdealermanagement.dto.account.password.ChangePasswordRequest;
import com.evdealer.evdealermanagement.dto.account.password.PasswordResponse;
import com.evdealer.evdealermanagement.service.implement.ChangePasswordService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class PasswordController {

    private final ChangePasswordService changePasswordService;

    @PutMapping("/change-password")
<<<<<<< HEAD
    @PreAuthorize("hasRole('MEMBER')")
=======
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER', 'STAFF')")
>>>>>>> 6a1e1487c410c85a24db0bb9490839aeade0c004
    public PasswordResponse changePassword(@Valid @RequestBody ChangePasswordRequest req,
            Authentication auth) {
        return changePasswordService.changePassword(auth.getName(), req);
    }
}