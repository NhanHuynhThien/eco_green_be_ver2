package com.evdealer.evdealermanagement.controller.staff;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evdealer.evdealermanagement.dto.rate.ApprovalRateResponse;
import com.evdealer.evdealermanagement.service.implement.StaffService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @GetMapping("/approval-rate")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<ApprovalRateResponse> getApprovalRate() {
        return ResponseEntity.ok(staffService.getApprovalRate());
    }

}
