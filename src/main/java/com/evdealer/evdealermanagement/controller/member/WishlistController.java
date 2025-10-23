package com.evdealer.evdealermanagement.controller.member;

import com.evdealer.evdealermanagement.dto.account.custom.CustomAccountDetails;
import com.evdealer.evdealermanagement.dto.wishlist.WishlistItemResponse;
import com.evdealer.evdealermanagement.dto.common.PageResponse;
import com.evdealer.evdealermanagement.dto.wishlist.WishlistRequest;
import com.evdealer.evdealermanagement.service.implement.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("member/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public PageResponse<WishlistItemResponse> listWishlist(@AuthenticationPrincipal CustomAccountDetails user,
                                                           @PageableDefault(size = 10, sort = "addedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return wishlistService.listWishlistItem(user.getAccountId(), pageable);
    }

    @PostMapping
    public void addWishlist(Authentication authentication, @RequestBody WishlistRequest request) {

        CustomAccountDetails user = (CustomAccountDetails) authentication.getPrincipal();
        wishlistService.addWishlistItem(user.getAccountId(), request.getProductId());
    }

    @DeleteMapping("/{productId}")
    public void removeWishlist(@AuthenticationPrincipal CustomAccountDetails user, @PathVariable String productId) {
        wishlistService.removeWishlistItem(user.getAccountId(), String.valueOf(productId));
    }
}
