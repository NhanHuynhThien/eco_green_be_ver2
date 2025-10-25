package com.evdealer.evdealermanagement.controller.staff;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evdealer.evdealermanagement.dto.account.count.MemberCountResponse;
import com.evdealer.evdealermanagement.dto.post.count.PostCountResponse;
import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.repository.AccountRepository;
import com.evdealer.evdealermanagement.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/staff/member")
@RequiredArgsConstructor
public class StaffMemberController {
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;

    @GetMapping("/{memberId}/posts/count")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<PostCountResponse> countMemberPosts(@PathVariable String memberId) {
        long count = productRepository.countBySeller_Id(memberId);
        return ResponseEntity.ok(new PostCountResponse(memberId, count));
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<MemberCountResponse> countMembers() {
        long count = accountRepository.countByRole(Account.Role.MEMBER);
        return ResponseEntity.ok(new MemberCountResponse("MEMBER", count));
    }
}
