package com.evdealer.evdealermanagement.controller.member;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evdealer.evdealermanagement.service.contract.IAccountService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/member/profile")
@RequiredArgsConstructor
public class MemberAccountController {

    private final IAccountService accountService;

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}
