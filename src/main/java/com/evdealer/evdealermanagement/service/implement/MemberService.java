package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.product.detail.ProductDetail;
import com.evdealer.evdealermanagement.entity.product.Product;
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


}
