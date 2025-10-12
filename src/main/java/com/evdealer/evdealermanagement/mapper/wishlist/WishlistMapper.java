package com.evdealer.evdealermanagement.mapper.wishlist;

import com.evdealer.evdealermanagement.dto.wishlist.WishlistItemResponse;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.entity.product.ProductImages;
import com.evdealer.evdealermanagement.entity.wishlist.WishlistItem;

public class WishlistMapper {

    public static WishlistItemResponse mapToWishlistItemResponse(WishlistItem wishlistItem) {
        Product product = wishlistItem.getProduct();
        return WishlistItemResponse.builder()
                .productId(product.getId())
                .productName(product.getTitle())
                .thumbnailUrl(getThumbnailUrl(product))
                .addedAt(wishlistItem.getAddedAt())
                .isWishlisted(true)
                .build();
    }

    private static String getThumbnailUrl(Product product) {
        if (product == null || product.getImages() == null) {
            return null;
        }
        String imageUrl =  product.getImages().stream()
                .filter(ProductImages::getIsPrimary)
                .map(ProductImages::getImageUrl)
                .findFirst()
                .orElse(
                        product.getImages().stream()
                                .map(ProductImages::getImageUrl)
                                .findFirst()
                                .orElse(null)
                );

        if(imageUrl.contains("res.cloudinary.com")) {
            return  imageUrl.replace("upload", "upload/w_108,h_108,c_fill,q_auto,f_auto/");
        }
        return imageUrl;
    }
}