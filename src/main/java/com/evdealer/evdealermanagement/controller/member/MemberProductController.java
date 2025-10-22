package com.evdealer.evdealermanagement.controller.member;

import com.evdealer.evdealermanagement.dto.account.custom.CustomAccountDetails;
import com.evdealer.evdealermanagement.dto.product.detail.ProductDetail;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.service.implement.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member/product")
public class MemberProductController {

    private final MemberService memberService;

    @GetMapping
    public List<ProductDetail> getProductsByStatus(
            Authentication authentication,
            @RequestParam Product.Status status
    ) {
        // Lấy thông tin người dùng hiện tại từ JWT token
        CustomAccountDetails customAccountDetails = (CustomAccountDetails) authentication.getPrincipal();

        String sellerId = customAccountDetails.getAccountId();

        return memberService.getProductsByStatus(sellerId, status);
    }
}
