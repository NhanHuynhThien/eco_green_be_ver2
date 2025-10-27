package com.evdealer.evdealermanagement.service.implement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evdealer.evdealermanagement.dto.payment.MomoRequest;
import com.evdealer.evdealermanagement.dto.payment.VnpayRequest;
import com.evdealer.evdealermanagement.dto.product.renewal.ProductRenewalRequest;
import com.evdealer.evdealermanagement.dto.product.renewal.ProductRenewalResponse;
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

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductRenewalService {

    private final ProductRepository productRepository;
    private final PostPackageRepository packageRepo;
    private final PostPackageOptionRepository optionRepo;
    private final PostPaymentRepository postPaymentRepository;
    private final VnpayService vnpayService;
    private final MomoService momoService;

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private LocalDateTime nowVietNam() {
        return ZonedDateTime.now(VIETNAM_ZONE).toLocalDateTime();
    }

    @Transactional
    public ProductRenewalResponse renewalProduct(String productId, ProductRenewalRequest req) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.getStatus() != Product.Status.ACTIVE && product.getStatus() != Product.Status.EXPIRED) {
            throw new AppException(ErrorCode.PACKAGE_INVALID_STATUS,
                    "Only renewal when the post is ACTIVE or EXPIRED");
        }

        if ((req.getStandardPackageId() == null || req.getStandardPackageId().isBlank()) &&
                (req.getAddonPackageId() == null || req.getAddonPackageId().isBlank())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Phải chọn ít nhất một gói để gia hạn");
        }

        BigDecimal totalPayable = BigDecimal.ZERO;
        PostPackage standardPkg = null;
        PostPackage addonPkg = null;
        PostPackageOption addonOpt = null;

        Integer standardDays = null;
        Integer addonDays = null;

        // ===== Gia hạn gói đăng tin=====
        if (req.getStandardPackageId() != null && !req.getStandardPackageId().isBlank()) {
            standardPkg = packageRepo.findById(req.getStandardPackageId())
                    .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_NOT_FOUND));

            if (!"STANDARD".equalsIgnoreCase(standardPkg.getCode()))
                throw new AppException(ErrorCode.INVALID_ID_PACKAGE,
                        "standardPackageId must be the STANDARD package");

            if (standardPkg.getStatus() != PostPackage.Status.ACTIVE)
                throw new AppException(ErrorCode.PACKAGE_INACTIVE);

            totalPayable = totalPayable.add(standardPkg.getPrice() != null ? standardPkg.getPrice() : BigDecimal.ZERO);

            standardDays = (standardPkg.getBaseDurationDays() != null && standardPkg.getBaseDurationDays() > 0)
                    ? standardPkg.getBaseDurationDays()
                    : 30;
        }

        // ===== Gia hạn gói ưu tiên / đặc biệt =====
        if (req.getAddonPackageId() != null && !req.getAddonPackageId().isBlank()) {
            addonPkg = packageRepo.findById(req.getAddonPackageId())
                    .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_NOT_FOUND));

            if (addonPkg.getStatus() != PostPackage.Status.ACTIVE)
                throw new AppException(ErrorCode.PACKAGE_INACTIVE);

            if (req.getOptionId() == null || req.getOptionId().isBlank())
                throw new AppException(ErrorCode.PACKAGE_OPTION_REQUIRED);

            addonOpt = optionRepo.findById(req.getOptionId())
                    .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_OPTION_NOT_FOUND));

            if (!addonOpt.getPostPackage().getId().equals(addonPkg.getId()))
                throw new AppException(ErrorCode.PACKAGE_OPTION_NOT_BELONG_TO_PACKAGE);

            totalPayable = totalPayable.add(addonOpt.getPrice() != null ? addonOpt.getPrice() : BigDecimal.ZERO);

            addonDays = (addonOpt.getDurationDays() != null ? addonOpt.getDurationDays() : 0);
        }

        if (standardDays != null && addonDays != null) {
            if (addonDays >= standardDays) {
                throw new AppException(ErrorCode.BAD_REQUEST,
                        "The number of days of the priority/special package must be less than the number of days of the (STANDARD) package");
            }
        }

        // Chọn package để lưu vào payment (đảm bảo package_id không null)
        PostPackage paymentPkg = (standardPkg != null) ? standardPkg : addonPkg;

        // ===== Tạo bản ghi thanh toán =====
        PostPayment payment = PostPayment.builder()
                .accountId(product.getSeller().getId())
                .productId(product.getId())
                .postPackage(paymentPkg)
                .postPackageOption(addonOpt)
                .amount(totalPayable)
                .paymentMethod(req.getPaymentMethod() != null
                        ? PostPayment.PaymentMethod.valueOf(req.getPaymentMethod().toUpperCase())
                        : null)
                .paymentStatus(totalPayable.signum() == 0
                        ? PostPayment.PaymentStatus.COMPLETED
                        : PostPayment.PaymentStatus.PENDING)
                .createdAt(nowVietNam())
                .build();
        postPaymentRepository.save(payment);

        // ====== Xử lý thanh toán ======
        String paymentUrl = null;
        if (totalPayable.signum() > 0 && req.getPaymentMethod() != null) {
            long amountVND = totalPayable.setScale(0, RoundingMode.HALF_UP).longValue();
            String method = req.getPaymentMethod().toUpperCase();

            if ("VNPAY".equals(method)) {
                paymentUrl = vnpayService.createPayment(
                        new VnpayRequest(payment.getId(), String.valueOf(amountVND))).getPaymentUrl();
            } else if ("MOMO".equals(method)) {
                paymentUrl = momoService.createPaymentRequest(
                        new MomoRequest(payment.getId(), String.valueOf(amountVND))).getPayUrl();
            } else {
                throw new IllegalArgumentException("Unsupported payment method: " + req.getPaymentMethod());
            }
        }

        return ProductRenewalResponse.builder()
                .productId(product.getId())
                .status(product.getStatus())
                .totalPayable(totalPayable)
                .currency("VND")
                .paymentUrl(paymentUrl)
                .updatedAt(nowVietNam())
                .build();
    }

    @Transactional
    public void handlePaymentCallbackFromRenewal(String paymentId, boolean success) {
        log.info("🔄 Processing payment callback - PaymentId: {}, Success: {}", paymentId, success);

        // ===== Lấy payment & product =====
        PostPayment payment = postPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        Product product = productRepository.findById(payment.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        // ===== Kiểm tra trạng thái thanh toán =====
        if (payment.getPaymentStatus() == PostPayment.PaymentStatus.COMPLETED ||
                payment.getPaymentStatus() == PostPayment.PaymentStatus.FAILED) {
            log.warn("Payment already processed with status: {}", payment.getPaymentStatus());
            return;
        }

        // ===== Nếu thanh toán thất bại =====
        if (!success) {
            payment.setPaymentStatus(PostPayment.PaymentStatus.FAILED);
            if (product.getStatus() == Product.Status.ACTIVE) {
                product.setStatus(Product.Status.ACTIVE);
            } else {
                product.setStatus(Product.Status.EXPIRED);
            }
            postPaymentRepository.save(payment);
            productRepository.save(product);
            log.warn("Payment failed - reverted to DRAFT if new post");
            return;
        }

        // ===== Thanh toán thành công =====
        payment.setPaymentStatus(PostPayment.PaymentStatus.COMPLETED);
        product.setPostingFee(product.getPostingFee() == null
                ? payment.getAmount()
                : product.getPostingFee().add(payment.getAmount()));

        LocalDateTime now = nowVietNam();
        int featuredDays = 0;

        // Nếu có option → cộng thêm ngày featured
        if (payment.getPostPackageOption() != null) {
            Integer d = payment.getPostPackageOption().getDurationDays();
            featuredDays = (d != null ? d : 0);
        }

        // ===== Cập nhật hạn featured & hạn đăng tin =====
        LocalDateTime currentFeatured = product.getFeaturedEndAt();
        LocalDateTime baseFeatured = (currentFeatured != null && currentFeatured.isAfter(now))
                ? currentFeatured
                : now;

        if (featuredDays > 0) {
            product.setFeaturedEndAt(baseFeatured.plusDays(featuredDays));
        }

        boolean extendExpire = shouldExtendExpire(payment);

        // Nếu gói thường → cộng 30 ngày
        if (extendExpire) {
            LocalDateTime baseExpire = (product.getExpiresAt() != null && product.getExpiresAt().isAfter(now))
                    ? product.getExpiresAt()
                    : now;
            product.setExpiresAt(baseExpire.plusDays(30));
            log.info("Extended expiresAt by 30 days (STANDARD)");
        } else {
            log.info("⏸ Skipped extending expiresAt (PRIORITY/SPECIAL)");
        }
        // ===== Cập nhật trạng thái bài đăng =====
        if (product.getStatus() == Product.Status.ACTIVE ||
                product.getStatus() == Product.Status.EXPIRED) {
            product.setStatus(Product.Status.ACTIVE);
        }

        // ===== Lưu thay đổi =====
        postPaymentRepository.save(payment);
        productRepository.save(product);

        log.info("""
                    Payment COMPLETED:
                 - Product: {}
                 - New Status: {}
                 - Expires At: {}
                 - Featured Until: {}
                 - Total Fee: {}
                """,
                product.getId(), product.getStatus(),
                product.getExpiresAt(), product.getFeaturedEndAt(),
                product.getPostingFee());
    }

    private boolean shouldExtendExpire(PostPayment p) {
        PostPackage pkg = p.getPostPackage();
        if (pkg == null)
            return false;
        return "STANDARD".equalsIgnoreCase(pkg.getCode());
    }

}
