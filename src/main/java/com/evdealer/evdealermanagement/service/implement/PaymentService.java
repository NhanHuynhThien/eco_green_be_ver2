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
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final ProductRepository productRepository;
    private final PostPackageRepository postPackageRepository;
    private final PostPaymentRepository postPaymentRepository;
    private final VnpayService vnpayService;
    private final MomoService momoService;

    // --- Constants ---
    private static final BigDecimal BASE_DISPLAY_PRICE_PER_30_DAYS = new BigDecimal("10000");
    private static final BigDecimal PRIORITY_FEATURE_PRICE_PER_30_DAYS = new BigDecimal("20000");
    private static final BigDecimal SPECIAL_FEATURE_PRICE_PER_30_DAYS = new BigDecimal("35000");
    private static final BigDecimal DAYS_IN_MONTH = new BigDecimal("30");

    /**
     * Chọn gói đăng tin và tạo yêu cầu thanh toán (VNPay / Momo)
     */
    public PackageResponse choosePackage(String productId, PackageRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.getStatus() != Product.Status.DRAFT) {
            throw new AppException(ErrorCode.PRODUCT_NOT_DRAFT);
        }

        PostPackage pkg = postPackageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_NOT_FOUND));

        // Tính tổng số tiền phải trả
        BigDecimal totalPayable = calculateTotalPayable(request, pkg);

        // Cập nhật trạng thái sản phẩm
        product.setStatus(Product.Status.PENDING_PAYMENT);
        productRepository.save(product);

        // Lưu thông tin thanh toán
        PostPayment payment = PostPayment.builder()
                .accountId(product.getSeller().getId())
                .productId(product.getId())
                .packageId(pkg.getId())
                .amount(totalPayable)
                .paymentMethod(PostPayment.PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()))
                .paymentStatus(PostPayment.PaymentStatus.PENDING)
                .build();
        postPaymentRepository.save(payment);

        // Tạo link thanh toán tương ứng
        String paymentUrl;
        try {
            if ("VNPAY".equalsIgnoreCase(request.getPaymentMethod())) {
                VnpayResponse res = vnpayService.createPayment(new VnpayRequest(payment.getId(), totalPayable.toString()));
                paymentUrl = res.getPaymentUrl();
            } else if ("MOMO".equalsIgnoreCase(request.getPaymentMethod())) {
                MomoResponse res = momoService.createPaymentRequest(new MomoRequest(payment.getId(), totalPayable.toString()));
                paymentUrl = res.getPayUrl();
            } else {
                throw new IllegalArgumentException("Unsupported payment method: " + request.getPaymentMethod());
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Encoding error when creating payment URL", e);
        }

        return PackageResponse.builder()
                .productId(product.getId())
                .status(product.getStatus())
                .totalPayable(totalPayable)
                .currency("VND")
                .paymentUrl(paymentUrl)
                .build();
    }

    /**
     * Tính tổng số tiền phải trả cho gói và số ngày chọn
     */
    @NotNull
    private static BigDecimal calculateTotalPayable(PackageRequest request, PostPackage pkg) {
        if (request.getDurationDays() == null || request.getDurationDays() <= 0) {
            throw new AppException(ErrorCode.DURATION_DAYS_MORE_THAN_ZERO);
        }

        BigDecimal duration = BigDecimal.valueOf(request.getDurationDays());

        // Phí hiển thị cơ bản mỗi ngày
        BigDecimal basePricePerDay = BASE_DISPLAY_PRICE_PER_30_DAYS
                .divide(DAYS_IN_MONTH, 2, RoundingMode.HALF_UP);
        BigDecimal displayCost = basePricePerDay.multiply(duration);

        // Phí tính năng gói (Priority / Special / etc.)
        BigDecimal featureCost = calculateFeatureCost(pkg, duration);

        return displayCost.add(featureCost).setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Tính phí tính năng của gói theo tên gói
     */
    @NotNull
    private static BigDecimal calculateFeatureCost(PostPackage pkg, BigDecimal duration) {
        String name = pkg.getName().trim().toUpperCase();
        BigDecimal pricePer30Days;

        switch (name) {
            case "PRIORITY":
            case "PRIORITY PACKAGE":
                pricePer30Days = PRIORITY_FEATURE_PRICE_PER_30_DAYS;
                break;
            case "SPECIAL":
            case "SPECIAL PACKAGE":
            case "PREMIUM":
                pricePer30Days = SPECIAL_FEATURE_PRICE_PER_30_DAYS;
                break;
            case "BASIC":
            case "DEFAULT":
            case "NORMAL":
                pricePer30Days = BigDecimal.ZERO;
                break;
            default:
                throw new AppException(ErrorCode.PACKAGE_NOT_FOUND);
        }

        BigDecimal pricePerDay = pricePer30Days.divide(DAYS_IN_MONTH, 2, RoundingMode.HALF_UP);
        return pricePerDay.multiply(duration);
    }

    /**
     * Xử lý callback thanh toán từ VNPay/Momo
     */
    public void handlePaymentCallback(String paymentId, boolean success) {
        PostPayment payment = postPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        Product product = productRepository.findById(payment.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (payment.getPaymentStatus() == PostPayment.PaymentStatus.COMPLETED) {
            return; // Đã hoàn tất rồi thì bỏ qua
        }

        if (success) {
            payment.setPaymentStatus(PostPayment.PaymentStatus.COMPLETED);
            product.setStatus(Product.Status.PENDING_REVIEW);

            PostPackage pkg = postPackageRepository.findById(payment.getPackageId())
                    .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_NOT_FOUND));

            int durationDays;
            if (pkg.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                durationDays = payment.getAmount()
                        .divide(pkg.getPrice(), 0, RoundingMode.DOWN)
                        .intValue();
            } else {
                durationDays = 30;
            }

            product.setExpiresAt(LocalDateTime.now().plusDays(durationDays));
        } else {
            payment.setPaymentStatus(PostPayment.PaymentStatus.FAILED);
            product.setStatus(Product.Status.DRAFT);
        }

        postPaymentRepository.save(payment);
        productRepository.save(product);
    }

    //Find and show all package
    public List<PostPackage> getAllPostPackages() {
        return postPackageRepository.findAll();
    }
}
