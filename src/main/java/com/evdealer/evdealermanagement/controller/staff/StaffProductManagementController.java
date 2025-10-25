package com.evdealer.evdealermanagement.controller.staff;

import com.evdealer.evdealermanagement.exceptions.AppException;
import com.evdealer.evdealermanagement.exceptions.ErrorCode;
import com.evdealer.evdealermanagement.service.implement.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/staff/product")
@RequiredArgsConstructor
public class StaffProductManagementController {

    private final AdminService adminService;

    @GetMapping("/all-posting-fee")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<String> getAllPostingFee() {
        try {
            String totalPostingFee = adminService.getTotalFee();
            return ResponseEntity.ok().body("totalFee: " + totalPostingFee);
        } catch (Exception e) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
    }
}
