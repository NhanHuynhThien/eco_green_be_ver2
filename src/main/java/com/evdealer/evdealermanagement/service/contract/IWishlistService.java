package com.evdealer.evdealermanagement.service.contract;

import com.evdealer.evdealermanagement.dto.wishlist.WishlistItemResponse;
import com.evdealer.evdealermanagement.dto.wishlist.WishlistPageResponse;
import org.springframework.data.domain.Pageable;

public interface IWishlistService {
    void addWishlistItem(String accountId, String productId);
    void removeWishlistItem(String accountId, String productId);
    WishlistPageResponse<WishlistItemResponse> listWishlistItem(String accountId, Pageable pageable);
}