package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.product.detail.ProductDetail;
import com.evdealer.evdealermanagement.dto.product.status.ProductStatusResponse;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.exceptions.AppException;
import com.evdealer.evdealermanagement.exceptions.ErrorCode;
import com.evdealer.evdealermanagement.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final ProductRepository productRepository;
    private final AuthenticationManager authentication;

    @Transactional
    public List<ProductDetail> getProductsByStatus(String sellerId, Product.Status status) {
        return productRepository.findBySellerAndStatus(sellerId, status)
                .stream().map(ProductDetail::fromEntity).toList();
    }

    public ProductStatusResponse markSold(String memberId, String productId) {
        Product p = productRepository
                .findByIdAndSellerId(productId, memberId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (p.getStatus() == Product.Status.SOLD) {
            return ProductStatusResponse.builder().id(p.getId()).status(p.getStatus()).build();
        }

        if (!(p.getStatus() == Product.Status.ACTIVE)) {
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        p.setStatus(Product.Status.SOLD);
        productRepository.save(p);

        return ProductStatusResponse.builder().id(p.getId()).status(p.getStatus()).build();
    }
}
