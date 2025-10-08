package com.evdealer.evdealermanagement.controller.admin;

import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.service.implement.AdminService;
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

    @GetMapping("/manage/account")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(adminService.getAllAccounts());
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
}
