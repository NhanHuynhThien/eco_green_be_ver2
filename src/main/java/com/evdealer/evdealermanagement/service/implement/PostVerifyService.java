package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.post.verification.PostVerifyRequest;
import com.evdealer.evdealermanagement.dto.post.verification.PostVerifyResponse;
import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.mapper.post.PostVerifyMapper;
import com.evdealer.evdealermanagement.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PostVerifyService {

    private final ProductRepository productRepository;
    private final UserContextService userContextService;

    @Transactional
    public PostVerifyResponse verifyPost(String productId, PostVerifyRequest request) {

        // 1) Load product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        // 2) Chỉ cho phép xử lý nếu đang ở trạng thái PENDING_REVIEW
        if (product.getStatus() != Product.Status.PENDING_REVIEW) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only products in PENDING_REVIEW status can be verified or rejected");
        }

        // 4) Lấy staff
        Account staff = userContextService.getCurrentUser()
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Staff not found or unauthorized"));

        // 5) Áp dụng hành động
        switch (request.getAction()) {
            case ACTIVE -> {
                product.setStatus(Product.Status.ACTIVE);
                product.setRejectReason(null);
            }
            case REJECT -> {
                product.setStatus(Product.Status.REJECTED);
                product.setRejectReason(request.getRejectReason());
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported action");
        }

        product.setApprovedBy(staff);
        product.setUpdatedAt(LocalDateTime.now());

        // 7) Lưu DB
        productRepository.save(product);

        // 8) Map response
        return PostVerifyMapper.mapToPostVerifyResponse(product);
    }
}
