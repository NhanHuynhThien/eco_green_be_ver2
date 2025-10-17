package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.post.verification.PostVerifyRequest;
import com.evdealer.evdealermanagement.dto.post.verification.PostVerifyResponse;
import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.mapper.post.PostVerifyMapper;
import com.evdealer.evdealermanagement.repository.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
public class StaffService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserContextService userContextService;

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

        /// 2) Lấy user hiện tại
        Account currentUser = userContextService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized"));

        // 3) Bắt buộc phải là STAFF (nếu muốn cho ADMIN làm luôn, xem phần ghi chú bên
        // dưới)
        if (currentUser.getRole() != Account.Role.STAFF) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only STAFF can verify posts");
        }

        // 5) Áp dụng hành động
        if (request.getAction() == PostVerifyRequest.ActionType.ACTIVE) {
            product.setStatus(Product.Status.ACTIVE);
            product.setRejectReason(null);
        } else if (request.getAction() == PostVerifyRequest.ActionType.REJECT) {
            product.setStatus(Product.Status.REJECTED);
            product.setRejectReason(request.getRejectReason());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported action");
        }

        product.setApprovedBy(currentUser);
        product.setUpdatedAt(LocalDateTime.now());

        // 7) Lưu DB
        productRepository.save(product);

        // 8) Map response
        return PostVerifyMapper.mapToPostVerifyResponse(product);
    }

}
