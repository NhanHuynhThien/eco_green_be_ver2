package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.transactions.*;
import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.entity.transactions.PurchaseRequest;
import com.evdealer.evdealermanagement.exceptions.AppException;
import com.evdealer.evdealermanagement.exceptions.ErrorCode;
import com.evdealer.evdealermanagement.repository.ProductRepository;
import com.evdealer.evdealermanagement.repository.PurchaseRequestRepository;
import com.evdealer.evdealermanagement.service.implement.EmailService;
import com.evdealer.evdealermanagement.service.implement.EversignService;
import com.evdealer.evdealermanagement.service.implement.UserContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;


@Service
@Slf4j
@RequiredArgsConstructor
public class PurchaseRequestService {

    private final PurchaseRequestRepository purchaseRequestRepository;
    private final ProductRepository productRepository;
    private final UserContextService userContextService;
    private final EversignService eversignService;
    private final EmailService emailService;

    @Transactional
    public PurchaseRequestResponse createPurchaseRequest(CreatePurchaseRequestDTO dto) {
        Account buyer = userContextService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        // ✅ Validate offeredPrice không null
        if (dto.getOfferedPrice() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Offered price is required");
        }

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (product.getStatus() != Product.Status.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product is not available for purchase");
        }

        if (product.getSeller().getId().equals(buyer.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot purchase your own product");
        }

        purchaseRequestRepository.findActivePurchaseRequest(product.getId(), buyer.getId())
                .ifPresent(pr -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "You already have an active purchase request for this product");
                });

        PurchaseRequest request = PurchaseRequest.builder()
                .product(product)
                .buyer(buyer)
                .seller(product.getSeller())
                .offeredPrice(dto.getOfferedPrice()) // ✅ Set giá trị
                .buyerMessage(dto.getBuyerMessage())
                .status(PurchaseRequest.RequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        PurchaseRequest savedRequest = purchaseRequestRepository.save(request);

        log.info("✅ Purchase request created. ID: {}, Product: {}, Buyer: {}, Price: {}",
                savedRequest.getId(), product.getId(), buyer.getId(), savedRequest.getOfferedPrice());

        // ✅ Gửi email async - không throw exception nếu fail
        try {
            emailService.sendPurchaseRequestNotification(
                    product.getSeller().getEmail(),
                    buyer.getFullName(),
                    product.getTitle(),
                    dto.getOfferedPrice());
        } catch (Exception e) {
            log.error("⚠️ Failed to send email notification, but request was saved: {}", e.getMessage());
        }

        return mapToResponse(savedRequest);
    }

    @Transactional
    public PurchaseRequestResponse respondToPurchaseRequest(SellerResponseDTO dto) {
        Account seller = userContextService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        PurchaseRequest request = purchaseRequestRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        if (!request.getSeller().getId().equals(seller.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the seller of this product");
        }

        if (request.getStatus() != PurchaseRequest.RequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Request has already been responded to");
        }

        if (dto.getAccept()) {
            return handleAcceptRequest(request, dto.getResponseMessage());
        } else {
            return handleRejectRequest(request, dto.getRejectReason());
        }
    }

    private PurchaseRequestResponse handleAcceptRequest(PurchaseRequest request, String responseMessage) {
        Product product = request.getProduct();

        product.setStatus(Product.Status.HIDDEN);
        productRepository.save(product);
        log.info("🔒 Product {} hidden after seller acceptance", product.getId());

        request.setStatus(PurchaseRequest.RequestStatus.ACCEPTED);
        request.setSellerResponseMessage(responseMessage);
        request.setRespondedAt(LocalDateTime.now());

        try {
            ContractInfoDTO contractInfo = eversignService.createAndSendContract(
                    request.getBuyer(),
                    request.getSeller(),
                    product,
                    request.getOfferedPrice());

            request.setContractId(contractInfo.getContractId());
            request.setContractUrl(contractInfo.getContractUrl());
            request.setStatus(PurchaseRequest.RequestStatus.CONTRACT_SENT);
            request.setContractStatus(PurchaseRequest.ContractStatus.SENT);

            log.info("📄 Contract created and sent. Contract ID: {}", contractInfo.getContractId());

        } catch (Exception e) {
            log.error("❌ Failed to create contract: {}", e.getMessage(), e);
            request.setStatus(PurchaseRequest.RequestStatus.ACCEPTED);
        }

        PurchaseRequest savedRequest = purchaseRequestRepository.save(request);

        try {
            emailService.sendPurchaseAcceptedNotification(
                    request.getBuyer().getEmail(),
                    request.getSeller().getFullName(),
                    product.getTitle(),
                    request.getContractUrl());
        } catch (Exception e) {
            log.error("⚠️ Failed to send email, but request was saved: {}", e.getMessage());
        }

        return mapToResponse(savedRequest);
    }

    private PurchaseRequestResponse handleRejectRequest(PurchaseRequest request, String rejectReason) {
        request.setStatus(PurchaseRequest.RequestStatus.REJECTED);
        request.setRejectReason(rejectReason);
        request.setRespondedAt(LocalDateTime.now());

        PurchaseRequest savedRequest = purchaseRequestRepository.save(request);

        try {
            emailService.sendPurchaseRejectedNotification(
                    request.getBuyer().getEmail(),
                    request.getSeller().getFullName(),
                    request.getProduct().getTitle(),
                    rejectReason);
        } catch (Exception e) {
            log.error("⚠️ Failed to send email, but request was saved: {}", e.getMessage());
        }

        log.info("❌ Purchase request rejected. ID: {}, Reason: {}",
                savedRequest.getId(), rejectReason);

        return mapToResponse(savedRequest);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseRequestResponse> getBuyerRequests(Pageable pageable) {
        Account buyer = userContextService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        Page<PurchaseRequest> requests = purchaseRequestRepository.findByBuyerId(buyer.getId(), pageable);
        return requests.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseRequestResponse> getSellerRequests(Pageable pageable) {
        Account seller = userContextService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        Page<PurchaseRequest> requests = purchaseRequestRepository.findBySellerId(seller.getId(), pageable);
        return requests.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public PurchaseRequestResponse getRequestDetail(String requestId) {
        Account currentUser = userContextService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        PurchaseRequest request = purchaseRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        if (!request.getBuyer().getId().equals(currentUser.getId()) &&
                !request.getSeller().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to view this request");
        }

        return mapToResponse(request);
    }

    @Transactional(readOnly = true)
    public long countPendingSellerRequests() {
        Account seller = userContextService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        return purchaseRequestRepository.countBySellerIdAndStatus(
                seller.getId(),
                PurchaseRequest.RequestStatus.PENDING);
    }

    @Transactional
    public void handleEversignWebhook(String contractId, String signerRole, LocalDateTime signedAt) {
        PurchaseRequest request = purchaseRequestRepository.findByContractId(contractId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        if ("buyer".equalsIgnoreCase(signerRole)) {
            request.setBuyerSignedAt(signedAt);
            if (request.getContractStatus() == PurchaseRequest.ContractStatus.SELLER_SIGNED) {
                request.setContractStatus(PurchaseRequest.ContractStatus.COMPLETED);
            } else {
                request.setContractStatus(PurchaseRequest.ContractStatus.BUYER_SIGNED);
            }
            log.info("✅ Buyer signed contract. Request ID: {}", request.getId());
        } else if ("seller".equalsIgnoreCase(signerRole)) {
            request.setSellerSignedAt(signedAt);
            if (request.getContractStatus() == PurchaseRequest.ContractStatus.BUYER_SIGNED) {
                request.setContractStatus(PurchaseRequest.ContractStatus.COMPLETED);
            } else {
                request.setContractStatus(PurchaseRequest.ContractStatus.SELLER_SIGNED);
            }
            log.info("✅ Seller signed contract. Request ID: {}", request.getId());
        }

        if (request.getBuyerSignedAt() != null && request.getSellerSignedAt() != null) {
            request.setStatus(PurchaseRequest.RequestStatus.COMPLETED);
            request.setContractStatus(PurchaseRequest.ContractStatus.COMPLETED);

            Product product = request.getProduct();
            product.setStatus(Product.Status.SOLD);
            productRepository.save(product);

            log.info("🎉 Contract completed. Product {} marked as SOLD", product.getId());

            try {
                emailService.sendContractCompletedNotification(
                        request.getBuyer().getEmail(),
                        request.getSeller().getEmail(),
                        product.getTitle());
            } catch (Exception e) {
                log.error("⚠️ Failed to send completion email: {}", e.getMessage());
            }
        }

        purchaseRequestRepository.save(request);
    }

    private PurchaseRequestResponse mapToResponse(PurchaseRequest request) {
        Product product = request.getProduct();
        String thumbnail = null;
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            thumbnail = product.getImages().get(0).getImageUrl();
        }

        return PurchaseRequestResponse.builder()
                .id(request.getId())
                .productId(product.getId())
                .productTitle(product.getTitle())
                .productPrice(product.getPrice())
                .buyerId(request.getBuyer().getId())
                .buyerName(request.getBuyer().getFullName())
                .buyerEmail(request.getBuyer().getEmail())
                .buyerPhone(request.getBuyer().getPhone())
                .sellerId(request.getSeller().getId())
                .sellerName(request.getSeller().getFullName())
                .sellerEmail(request.getSeller().getEmail())
                .sellerPhone(request.getSeller().getPhone())
                .offeredPrice(request.getOfferedPrice())
                .buyerMessage(request.getBuyerMessage())
                .sellerResponseMessage(request.getSellerResponseMessage())
                .status(request.getStatus().name())
                .contractStatus(request.getContractStatus() != null ? request.getContractStatus().name() : null)
                .contractUrl(request.getContractUrl())
                .contractId(request.getContractId())
                .rejectReason(request.getRejectReason())
                .createdAt(request.getCreatedAt())
                .respondedAt(request.getRespondedAt())
                .buyerSignedAt(request.getBuyerSignedAt())
                .sellerSignedAt(request.getSellerSignedAt())
                .build();
    }
}