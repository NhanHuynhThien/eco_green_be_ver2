package com.evdealer.evdealermanagement.controller.member;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.evdealer.evdealermanagement.entity.account.Account;
// import com.evdealer.evdealermanagement.service.implement.MemberService;

// import jakarta.validation.Valid;

// @RestController
// @RequestMapping("/member")
// public class MemberProfileController {
//     private final MemberService memberService;

//     public MemberProfileController(MemberService memberService) {
//         this.memberService = memberService;
//     }

//     @PutMapping("/profile/{id}")
//     public ResponseEntity<?> updateAccount(@Valid @RequestBody Account accountRequest, @PathVariable("id") Long id) {
//         return ResponseEntity.status(HttpStatus.OK).body(this.memberService.updateMemberProfile(accountRequest));
//     }

//     @DeleteMapping("/profile/{id}")
//     public ResponseEntity<Void> deleteAccount(@PathVariable("id") Long id) {
//         this.memberService.deleteAccount(id);
//         return ResponseEntity.ok(null);
//     }
// }

import com.evdealer.evdealermanagement.dto.account.profile.AccountProfileResponse;
import com.evdealer.evdealermanagement.dto.account.profile.AccountUpdateRequest;
import com.evdealer.evdealermanagement.service.contract.IAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member/profile")
@RequiredArgsConstructor
public class MemberProfileController {

    private final IAccountService accountService;

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEMBER','ADMIN')")
    public ResponseEntity<AccountProfileResponse> updateProfile(
            @PathVariable String id,
            @Valid @RequestBody AccountUpdateRequest request) {
        return ResponseEntity.ok(accountService.updateProfile(id, request));
    }
}