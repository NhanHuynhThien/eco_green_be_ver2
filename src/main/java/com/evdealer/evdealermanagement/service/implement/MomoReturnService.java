package com.evdealer.evdealermanagement.service.implement;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.entity.product.Product.Status;
import com.evdealer.evdealermanagement.repository.ProductRepository;
import com.evdealer.evdealermanagement.utils.MomoSignature;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MomoReturnService {

    private final MomoSignature momoSignature;
    private final ProductRepository productRepository;

    @Transactional
    public String handleReturnAndUpdateProduct(Map<String, String> params) {
        // 1) verify chữ ký
        if (!momoSignature.verify(params)) {
            return "INVALID_SIGNATURE";
        }

        // 2) kiểm tra resultCode
        String resultCode = params.getOrDefault("resultCode", "-1");
        if (!"0".equals(resultCode)) {
            return "PAYMENT_FAILED_" + resultCode;
        }

        // 3) lấy productId từ extraData
        String extraDataB64 = params.getOrDefault("extraData", "");
        if (extraDataB64.isEmpty()) {
            return "MISSING_EXTRA_DATA";
        }

        String productId;
        try {
            String json = new String(Base64.getDecoder().decode(extraDataB64), StandardCharsets.UTF_8);
            // json: {"productId":"e2bfffd9-d646-4cf4-89f1-c245c6923824"}
            Matcher m = Pattern.compile("\"productId\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
            if (!m.find()) {
                return "INVALID_EXTRA_DATA";
            }
            productId = m.group(1); // lấy chuỗi id
        } catch (Exception e) {
            return "INVALID_EXTRA_DATA";
        }

        // 4) cập nhật trạng thái product (idempotent)
        Product p = productRepository.findById(productId)
                .orElse(null);

        if (p == null) {
            return "PRODUCT_NOT_FOUND";
        }

        if (p.getStatus() == Status.PENDING_REVIEW || p.getStatus() == Status.SOLD) {
            return "OK_ALREADY_UPDATED";
        }

        p.setStatus(Status.PENDING_REVIEW);
        productRepository.save(p);

        return "OK";
    }
}
