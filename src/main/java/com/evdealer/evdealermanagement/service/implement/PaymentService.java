package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.payment.MomoRequest;
import com.evdealer.evdealermanagement.dto.payment.MomoResponse;
import com.evdealer.evdealermanagement.dto.payment.VnpayRequest;
import com.evdealer.evdealermanagement.dto.payment.VnpayResponse;
import com.evdealer.evdealermanagement.dto.post.packages.PackageRequest;
import com.evdealer.evdealermanagement.dto.post.packages.PackageResponse;
import com.evdealer.evdealermanagement.dto.post.packages.PostPackageOptionResponse;
import com.evdealer.evdealermanagement.dto.post.packages.PostPackageResponse;
import com.evdealer.evdealermanagement.entity.post.PostPackage;
import com.evdealer.evdealermanagement.entity.post.PostPackageOption;
import com.evdealer.evdealermanagement.entity.post.PostPayment;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.exceptions.AppException;
import com.evdealer.evdealermanagement.exceptions.ErrorCode;
import com.evdealer.evdealermanagement.repository.PostPackageOptionRepository;
import com.evdealer.evdealermanagement.repository.PostPackageRepository;
import com.evdealer.evdealermanagement.repository.PostPaymentRepository;
import com.evdealer.evdealermanagement.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final ProductRepository productRepository;
    private final PostPackageRepository packageRepo;
    private final PostPackageOptionRepository optionRepo;
    private final PostPaymentRepository postPaymentRepository;
    private final VnpayService vnpayService;
    private final MomoService momoService;

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private PostPayment build;

    private LocalDateTime nowVietNam () {
        return ZonedDateTime.now(VIETNAM_ZONE).toLocalDateTime();
    }

    @Transactional
    public PackageResponse choosePackage(String productId, PackageRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.getStatus() != Product.Status.DRAFT) {
            throw new AppException(ErrorCode.PRODUCT_NOT_DRAFT);
        }

        PostPackage pkg = packageRepo.findById(request.getPackageId())
                .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_NOT_FOUND));

        if (pkg.getStatus() != PostPackage.Status.ACTIVE) {
            throw new AppException(ErrorCode.PACKAGE_INACTIVE);
        }

        int desiredDays;
        BigDecimal totalPayable;

        if (pkg.getBillingMode() == PostPackage.BillingMode.FIXED) {
            desiredDays = pkg.getBaseDurationDays() != null ? pkg.getBaseDurationDays() : 30;
            totalPayable = pkg.getPrice();
        } else if (pkg.getBillingMode() == PostPackage.BillingMode.PER_DAY) {
            if (request.getOptionId() == null) {
                throw new AppException(ErrorCode.PACKAGE_OPTION_REQUIRED);
            }
            PostPackageOption option = optionRepo.findById(request.getOptionId())
                    .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_OPTION_NOT_FOUND));

            if (!option.getPostPackage().getId().equals(pkg.getId())) {
                throw new AppException(ErrorCode.PACKAGE_OPTION_NOT_BELONG_TO_PACKAGE);
            }

            desiredDays = option.getDurationDays();
            totalPayable = pkg.getPrice().add(option.getPrice());
        } else {
            throw new AppException(ErrorCode.PACKAGE_BILLING_MODE_INVALID);
        }

        boolean isFirstPost = !postPaymentRepository.existsByAccountIdAndPaymentStatus(
                product.getSeller().getId(),
                PostPayment.PaymentStatus.COMPLETED
        );

        if ("STANDARD".equalsIgnoreCase(pkg.getCode()) && isFirstPost) {
            totalPayable = BigDecimal.ZERO;
        }

        PostPayment payment = build;

        postPaymentRepository.save(payment);
        log.info("Payment saved with ID: {}", payment.getId());

        // update product status
        product.setStatus(totalPayable.signum() == 0
                ? Product.Status.PENDING_REVIEW
                : Product.Status.PENDING_PAYMENT);
        productRepository.save(product);

        String paymentUrl = null;
        if (totalPayable.signum() > 0 && request.getPaymentMethod() != null) {
            long amountVND = totalPayable.setScale(0, RoundingMode.HALF_UP).longValue();
            switch (request.getPaymentMethod().toUpperCase()) {
                case "VNPAY":
                    if (amountVND < 10000) throw new AppException(ErrorCode.USER_NOT_FOUND);
                    VnpayResponse vnpayResponse = vnpayService.createPayment(
                            new VnpayRequest(payment.getId(), String.valueOf(amountVND))
                    );
                    paymentUrl = vnpayResponse.getPaymentUrl();
                    break;
                case "MOMO":
                    MomoResponse momoResponse = momoService.createPaymentRequest(
                            new MomoRequest(payment.getId(), String.valueOf(amountVND))
                    );
                    paymentUrl = momoResponse.getPayUrl();
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported payment method: " + request.getPaymentMethod());
            }
        }

        return PackageResponse.builder()
                .productId(product.getId())
                .status(product.getStatus())
                .totalPayable(totalPayable)
                .currency("VND")
                .paymentUrl(paymentUrl)
                .build();
    }

    @Transactional
    public void handlePaymentCallback(String paymentId, boolean success) {
        log.info("🔄 Processing payment callback - PaymentId: {}, Success: {}", paymentId, success);

        PostPayment payment = postPaymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.error("Payment not found: {}", paymentId);
                    return new AppException(ErrorCode.PAYMENT_NOT_FOUND);
                });

        log.info("Payment found: ID={}, Status={}, Amount={}",
                payment.getId(), payment.getPaymentStatus(), payment.getAmount());

        Product product = productRepository.findById(payment.getProductId())
                .orElseThrow(() -> {
                    log.error("❌ Product not found: {}", payment.getProductId());
                    return new AppException(ErrorCode.PRODUCT_NOT_FOUND);
                });

        log.info("Product found: ID={}, Status={}", product.getId(), product.getStatus());

        // Skip nếu đã xử lý rồi
        if (payment.getPaymentStatus() == PostPayment.PaymentStatus.COMPLETED ||
                payment.getPaymentStatus() == PostPayment.PaymentStatus.FAILED) {
            log.warn("⚠️ Payment already processed with status: {}", payment.getPaymentStatus());
            return;
        }

        if (product.getStatus() == Product.Status.PENDING_PAYMENT) {
            if (success) {
                log.info("Payment successful - Updating to COMPLETED");
                payment.setPaymentStatus(PostPayment.PaymentStatus.COMPLETED);

                if (product.getPostingFee() == null) {
                    product.setPostingFee(payment.getAmount());
                } else {
                    product.setPostingFee(product.getPostingFee().add(payment.getAmount()));
                }
                product.setStatus(Product.Status.PENDING_REVIEW);

                log.info("💰 Posting fee updated: {}", product.getPostingFee());
                log.info("📊 Product status updated: {}", product.getStatus());
            } else {
                log.info("❌ Payment failed - Updating to FAILED");
                payment.setPaymentStatus(PostPayment.PaymentStatus.FAILED);
                product.setStatus(Product.Status.DRAFT);
            }
        } else {
            log.warn("⚠️ Product is not in PENDING_PAYMENT status: {}", product.getStatus());
        }

        postPaymentRepository.save(payment);
        productRepository.save(product);

        log.info("💾 Payment and Product saved successfully");
        log.info("📊 Final - Payment status: {}, Product status: {}",
                payment.getPaymentStatus(), product.getStatus());
    }

    public List<PostPackageResponse> getAllPackages() {
        var packages = packageRepo.findByStatusOrderByPriorityLevelDesc(PostPackage.Status.ACTIVE);

        return packages.stream().map(p -> {
            List<PostPackageOptionResponse> optionResponses = optionRepo
                    .findByPostPackage_IdAndStatusOrderBySortOrderAsc(p.getId(), PostPackageOption.Status.ACTIVE)
                    .stream()
                    .map(o -> PostPackageOptionResponse.builder()
                            .id(o.getId())
                            .name(o.getName())
                            .durationDays(o.getDurationDays())
                            .price(o.getPrice())
                            .listPrice(o.getListPrice())
                            .isDefault(o.getIsDefault())
                            .sortOrder(o.getSortOrder())
                            .build())
                    .toList();

            String note = "STANDARD".equals(p.getCode()) ? "Miễn phí lần đăng đầu tiên" : null;

            return PostPackageResponse.builder()
                    .postPackageId(p.getId())
                    .postPackageCode(p.getCode())
                    .postPackageName(p.getName())
                    .postPackageDesc(p.getDescription())
                    .billingMode(p.getBillingMode())
                    .category(p.getCategory())
                    .baseDurationDays(p.getBaseDurationDays())
                    .price(p.getPrice())
                    .dailyPrice(p.getDailyPrice())
                    .includesPostFee(p.getIncludesPostFee())
                    .priorityLevel(p.getPriorityLevel())
                    .badgeLabel(p.getBadgeLabel())
                    .showInLatest(p.getShowInLatest())
                    .showTopSearch(p.getShowTopSearch())
                    .listPrice(p.getListPrice())
                    .isDefault(p.getIsDefault())
                    .note(note)
                    .options(optionResponses)
                    .build();
        }).toList();
    }
}