package com.evdealer.evdealermanagement.controller.admin;

import com.evdealer.evdealermanagement.dto.account.register.AccountRegisterRequest;
import com.evdealer.evdealermanagement.dto.account.register.AccountRegisterResponse;
import com.evdealer.evdealermanagement.dto.account.response.ApiResponse;
import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.exceptions.ErrorCode;
import com.evdealer.evdealermanagement.service.implement.AdminService;
import com.evdealer.evdealermanagement.service.implement.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminAccountManagementController {

    private final AdminService adminService;
    private final AuthService authService;

    @GetMapping("/manage/account/member")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Account>> getMemberAccount() {
        return ResponseEntity.ok(adminService.getMemberAccounts());
    }

    @GetMapping("/manage/account/staff")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Account>> getStaffAccount() {
        return ResponseEntity.ok(adminService.getStaffAccounts());
    }

    @DeleteMapping("/manage/account/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteAccount(@PathVariable String id) {
        boolean deleted = adminService.deleteAccount(id);
        if (deleted) {
            return ResponseEntity.ok("Account deleted successfully");
        } else {
            return ResponseEntity.status(404).body("Account not found");
        }
    }

    @PutMapping("/manage/account/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> changeAccountStatus(
            @PathVariable String id,
            @RequestParam Account.Status status
    ) {
        boolean updated = adminService.changeStatusAccount(id, status);
        if (updated) {
            return ResponseEntity.ok("Account status updated successfully");
        } else {
            return ResponseEntity.status(404).body("Account not found");
        }
    }

    @PostMapping("/register/staff")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AccountRegisterResponse> registerStaffAccount(@Valid @RequestBody AccountRegisterRequest request) {
        AccountRegisterResponse response = authService.registerStaffAccount(request);
        return new ApiResponse<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), response);
    }
}
