package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.payment.MomoRequest;
import com.evdealer.evdealermanagement.dto.payment.MomoResponse;
import com.evdealer.evdealermanagement.dto.payment.VnpayRequest;
import com.evdealer.evdealermanagement.dto.payment.VnpayResponse;
import com.evdealer.evdealermanagement.dto.post.packages.PackageRequest;
import com.evdealer.evdealermanagement.dto.post.packages.PackageResponse;
import com.evdealer.evdealermanagement.entity.post.PostPackage;
import com.evdealer.evdealermanagement.entity.post.PostPayment;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.exceptions.AppException;
import com.evdealer.evdealermanagement.exceptions.ErrorCode;
import com.evdealer.evdealermanagement.repository.PostPackageRepository;
import com.evdealer.evdealermanagement.repository.PostPaymentRepository;
import com.evdealer.evdealermanagement.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final ProductRepository productRepository;
    private final PostPackageRepository postPackageRepository;
    private final PostPaymentRepository postPaymentRepository;
    private final VnpayService vnpayService;
    private final MomoService momoService;

    public PackageResponse choosePackage(String productId, PackageRequest request) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if(product.getStatus() != Product.Status.DRAFT) {
            throw new AppException(ErrorCode.PRODUCT_NOT_DRAFT);
        }

        PostPackage pkg = postPackageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_NOT_FOUND));

        if(request.getDurationDays() == null || request.getDurationDays() <= 0) {
            throw new AppException(ErrorCode.DURATION_DAYS_MORE_THAN_ZERO);
        }
        int duration =  request.getDurationDays();

        BigDecimal totalPayable;

        switch (pkg.getId()) {
            case "e88dd39f-a5ae-11f0-82a9-a2aad89b694c":
                totalPayable = new BigDecimal("5000").multiply(BigDecimal.valueOf(duration));
                break;
            case "e88dd641-a5ae-11f0-82a9-a2aad89b694c":
                totalPayable = new BigDecimal("20000").multiply(BigDecimal.valueOf(duration));
                break;
            case "e88dd6bb-a5ae-11f0-82a9-a2aad89b694c":
                totalPayable = new BigDecimal("35000").multiply(BigDecimal.valueOf(duration));
                break;
                default:
                    throw new AppException(ErrorCode.PACKAGE_NOT_FOUND);
        }

        product.setStatus(Product.Status.PENDING_PAYMENT);
        productRepository.save(product);

        PostPayment payment = PostPayment.builder()
                .accountId(product.getSeller().getId())
                .productId(product.getId())
                .packageId(pkg.getId())
                .amount(totalPayable)
                .paymentMethod(PostPayment.PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()))
                .paymentStatus(PostPayment.PaymentStatus.PENDING)
                .build();
        postPaymentRepository.save(payment);

        String paymentUrl;
        if("VNPAY".equalsIgnoreCase(request.getPaymentMethod())) {
            VnpayRequest vnpayReq =  new VnpayRequest(payment.getId(), totalPayable.toString());
            try {
                VnpayResponse res = vnpayService.createPayment(vnpayReq);
                paymentUrl = res.getPaymentUrl();
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Encoding error when creating VNPay payment", e);
            }
        } else if ("MOMO".equalsIgnoreCase(request.getPaymentMethod())) {
            MomoRequest momoReq = new MomoRequest(payment.getId(), totalPayable.toString());
            MomoResponse res = momoService.createPaymentRequest(momoReq);
            paymentUrl = res.getPayUrl();
        } else {
            throw new IllegalArgumentException("Unsupported payment method: " + request.getPaymentMethod());
        }

        return PackageResponse.builder()
                .productId(product.getId())
                .status(product.getStatus())
                .totalPayable(totalPayable)
                .currency("VND")
                .paymentUrl(paymentUrl)
                .build();
    }

    public void handlePaymentCallback(String paymentId, boolean success) {

        PostPayment payment = postPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        Product product = productRepository.findById(payment.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (payment.getPaymentStatus() == PostPayment.PaymentStatus.COMPLETED) {
            return;
        }

        if(success) {
            payment.setPaymentStatus(PostPayment.PaymentStatus.COMPLETED);

            product.setStatus(Product.Status.PENDING_REVIEW);

            PostPackage pkg = postPackageRepository.findById(payment.getPackageId())
                    .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_NOT_FOUND));

            int durationDays;
            if(pkg.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                durationDays = payment.getAmount().divide(pkg.getPrice(), 0, RoundingMode.DOWN).intValue();
            } else {
                durationDays = 1;
            }

            product.setExpiresAt(LocalDateTime.now().plusDays(durationDays));
        } else {
            payment.setPaymentStatus(PostPayment.PaymentStatus.FAILED);
            product.setStatus(Product.Status.PENDING_PAYMENT);
        }
        postPaymentRepository.save(payment);
        productRepository.save(product);
    }

}
