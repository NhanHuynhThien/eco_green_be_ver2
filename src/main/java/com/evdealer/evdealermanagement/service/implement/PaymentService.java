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

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final ProductRepository productRepository;
    private final PostPackageRepository postPackageRepository;
    private final PostPaymentRepository postPaymentRepository;
    private final VnpayService vnpayService;
    private final MomoService momoService;
    private final PostPackageRepository packageRepo;
    private final PostPackageOptionRepository optionRepo;

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
        // Xác định số ngày member muốn + số tiền phải trả từ DB
        int desiredDays;
        BigDecimal totalPayable;

        if (pkg.getBillingMode() == PostPackage.BillingMode.FIXED) {
            desiredDays = pkg.getBaseDurationDays() != null ? pkg.getBaseDurationDays() : 30; // STANDARD: 30 days
            totalPayable = pkg.getPrice();
        } else if (pkg.getBillingMode() == PostPackage.BillingMode.PER_DAY) {
            if (request.getOptionId() == null) {
                throw new AppException(ErrorCode.PACKAGE_OPTION_REQUIRED);
            }
            PostPackageOption ppo = optionRepo.findById(request.getOptionId())
                    .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_OPTION_NOT_FOUND));
            if (!ppo.getPostPackage().getId().equals(pkg.getId())) {
                throw new AppException(ErrorCode.PACKAGE_OPTION_NOT_BELONG_TO_PACKAGE);
            }
            desiredDays = ppo.getDurationDays();
            Optional<PostPackage> basePackage = packageRepo.findById("99948170-ae4c-11f0-82a9-a2aad89b694c");
            totalPayable = basePackage.get().getPrice().add(ppo.getPrice());
        } else {
            throw new AppException(ErrorCode.PACKAGE_BILLING_MODE_INVALID);
        }

        // Miễn phí lần đầu
        boolean isFirstPost = !postPaymentRepository.existsByAccountIdAndPaymentStatus(product.getSeller().getId(),
                PostPayment.PaymentStatus.COMPLETED);
        if ("STANDARD".equalsIgnoreCase(pkg.getCode()) && isFirstPost) {
            totalPayable = BigDecimal.ZERO;
        }
        // Save DB
        PostPayment payment = PostPayment.builder()
                .accountId(product.getSeller().getId())
                .productId(product.getId())
                .postPackage(packageRepo.findById(request.getPackageId())
                        .orElseThrow(() -> new AppException(ErrorCode.PACKAGE_NOT_FOUND)))
                .postPackageOption(optionRepo.findById(request.getOptionId()).orElse(null))
                .amount(totalPayable)
                .paymentMethod(PostPayment.PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()))
                .paymentStatus(totalPayable.signum() == 0 ? PostPayment.PaymentStatus.COMPLETED
                        : PostPayment.PaymentStatus.PENDING)
                .build();

        postPaymentRepository.save(payment);

        // update product status
        if (totalPayable.signum() == 0) {
            product.setStatus(Product.Status.PENDING_REVIEW);
        } else {
            product.setStatus(Product.Status.PENDING_PAYMENT);
        }
        productRepository.save(product);

        // Tạo link than toán
        log.info("DEBUG before payment URL: totalPayable={}, signum={}, method='{}'",
                totalPayable,
                (totalPayable == null ? "null" : totalPayable.signum()),
                request.getPaymentMethod());

        String paymentUrl = null;

        if (totalPayable != null && totalPayable.compareTo(BigDecimal.ZERO) > 0) {
            try {
                String method = request.getPaymentMethod() == null ? ""
                        : request.getPaymentMethod().trim().toUpperCase();
                switch (method) {
                    case "VNPAY" -> {
                        // Convert BigDecimal sang số nguyên (loại bỏ phần thập phân)
                        long amountInVND = totalPayable.setScale(0, RoundingMode.HALF_UP).longValue();

                        // Validate số tiền tối thiểu của VNPay
                        if (amountInVND < 10000) {
                            throw new AppException(ErrorCode.USER_NOT_FOUND);
                        }

                        VnpayResponse res = vnpayService.createPayment(
                                new VnpayRequest(payment.getId(), String.valueOf(amountInVND)));
                        paymentUrl = res.getPaymentUrl();
                        log.info("DEBUG vnpayService response: {}", res);
                        log.info("DEBUG vnpayService paymentUrl: {}", paymentUrl);
                    }

                    case "MOMO" -> {
                        MomoResponse res = momoService
                                .createPaymentRequest(new MomoRequest(payment.getId(), totalPayable.toPlainString()));
                        paymentUrl = res.getPayUrl();
                        log.info("DEBUG momoService response: {}", res);
                        log.info("DEBUG momoService payUrl: {}", (res == null ? "res=null" : res.getPayUrl()));

                    }

                    default -> throw new IllegalArgumentException("Unsupported payment method: " + method);
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Encoding error when creating payment URL", e);
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
        PostPayment payment = postPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        Product product = productRepository.findById(payment.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        // Tránh xử lý lại các callback đã kết thúc
        if (payment.getPaymentStatus() == PostPayment.PaymentStatus.COMPLETED ||
                payment.getPaymentStatus() == PostPayment.PaymentStatus.FAILED) {
            return;
        }

        if (product.getStatus() == Product.Status.PENDING_PAYMENT) {
            if (success) {
                payment.setPaymentStatus(PostPayment.PaymentStatus.COMPLETED);
                product.setStatus(Product.Status.PENDING_REVIEW);
            } else {
                payment.setPaymentStatus(PostPayment.PaymentStatus.FAILED);
                product.setStatus(Product.Status.DRAFT);
            }
        }

        postPaymentRepository.save(payment);
        productRepository.save(product);
    }

    private int resolveDaysFromOption(String optionId) {
        if (optionId == null) {
            return 0;
        }
        return optionRepo.findById(optionId)
                .map(PostPackageOption::getDurationDays).orElse(0);
    }

    // Find and show all package
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

    private String normalizeString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
