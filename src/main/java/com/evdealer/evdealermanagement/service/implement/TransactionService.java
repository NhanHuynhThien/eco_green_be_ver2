package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.payment.TransactionResponse;
import com.evdealer.evdealermanagement.entity.post.PostPackage;
import com.evdealer.evdealermanagement.entity.post.PostPackageOption;
import com.evdealer.evdealermanagement.entity.post.PostPayment;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.repository.PostPaymentRepository;
import com.evdealer.evdealermanagement.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final PostPaymentRepository postPaymentRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions() {
        List<PostPayment> payments = postPaymentRepository.findAllByOrderByCreatedAtDesc();

        return payments.stream().map(p -> {
            Product product = productRepository.findById(p.getProduct().getId()).orElse(null);
            PostPackage postPackage = p.getPostPackage();
            PostPackageOption postPackageOption = p.getPostPackageOption();
            return TransactionResponse.builder()
                    .paymentId(p.getId())
                    .createdAt(p.getCreatedAt())
                    .amount(p.getAmount())
                    .paymentMethod(p.getPaymentMethod().name())
                    .packageName(postPackage != null ? postPackage.getName() : null)
                    .durationDays(postPackageOption != null && postPackageOption.getDurationDays() != null
                            ? postPackageOption.getDurationDays()
                            : (postPackage != null ? postPackage.getDurationDays() : null))
                    .productId(p.getProduct().getId())
                    .productName(product != null ? product.getTitle() : null)
                    .build();
        }).toList();
    }
}
