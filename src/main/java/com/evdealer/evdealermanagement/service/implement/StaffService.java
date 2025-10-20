package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.post.verification.PostVerifyRequest;
import com.evdealer.evdealermanagement.dto.post.verification.PostVerifyResponse;
import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.entity.post.PostPayment;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.mapper.post.PostVerifyMapper;
import com.evdealer.evdealermanagement.repository.PostPaymentRepository;
import com.evdealer.evdealermanagement.repository.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @Autowired
    private PostPaymentRepository postPaymentRepository;

    @Transactional
    public PostVerifyResponse verifyPost(String productId, PostVerifyRequest request) {

        // 1) Lấy user hiện tại (phải có quyền STAFF hoặc ADMIN)
        Account currentUser = userContextService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized user"));

        if (currentUser.getRole() != Account.Role.STAFF && currentUser.getRole() != Account.Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only STAFF or ADMIN can verify posts");
        }

        // 2) Load product sau khi xác thực quyền
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        // 3) Chỉ cho phép xử lý nếu post đang ở trạng thái PENDING_REVIEW
        if (product.getStatus() != Product.Status.PENDING_REVIEW) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only posts in PENDING_REVIEW status can be verified or rejected");
        }

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
        productRepository.save(product);
        return PostVerifyMapper.mapToPostVerifyResponse(product);
    }

    @Transactional
    public List<PostVerifyResponse> getListVerifyPost() {
        List<Product> products = productRepository.findByStatus(Product.Status.PENDING_REVIEW);
        List<PostVerifyResponse> responses = new ArrayList<>();
        for (Product product : products) {
            // Lấy payment gần nhất có trạng thái COMPLETED
            PostPayment payment = postPaymentRepository
                    .findTopByProductIdAndPaymentStatusOrderByIdDesc(
                            product.getId(),
                            PostPayment.PaymentStatus.COMPLETED)
                    .orElse(null);
            PostVerifyResponse response = PostVerifyMapper.mapToPostVerifyResponse(product, payment);
            responses.add(response);
        }
        return responses;
    }

}
