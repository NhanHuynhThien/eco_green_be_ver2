package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.verification.VerificationActionRequest;
import com.evdealer.evdealermanagement.dto.verification.VerificationActionResponse;
import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.mapper.verification.VerificationActionMapper;
import com.evdealer.evdealermanagement.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProductVerificationService {

    private final ProductRepository productRepository;
    private final UserContextService userContextService;

    @Transactional
    public VerificationActionResponse verifyProduct(String productId,
            VerificationActionRequest request) {

        // 1) Load product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        // 3) Lưu trạng thái trước khi đổi
        Product.Status previous = product.getStatus();

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
                product.setStatus(
                        request.getAction() == VerificationActionRequest.ActionType.REJECT
                                ? Product.Status.REJECTED
                                : Product.Status.ACTIVE);
                product.setRejectReason(request.getRejectReason());
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported action");
        }

        product.setApprovedBy(staff);

        // 7) Lưu DB
        productRepository.save(product);

        // 8) Map response
        return VerificationActionMapper.mapToVerificationActionResponse(product, previous);
    }
}
