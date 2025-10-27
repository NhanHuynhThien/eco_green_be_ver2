package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.transactions.*;
import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.entity.transactions.PurchaseRequest;
import com.evdealer.evdealermanagement.exceptions.AppException;
import com.evdealer.evdealermanagement.exceptions.ErrorCode;
import com.evdealer.evdealermanagement.repository.ProductRepository;
import com.evdealer.evdealermanagement.repository.PurchaseRequestRepository;
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
                .offeredPrice(dto.getOfferedPrice())
                .buyerMessage(dto.getBuyerMessage())
                .status(PurchaseRequest.RequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        PurchaseRequest savedRequest = purchaseRequestRepository.save(request);
        log.info("Purchase request created: {}", savedRequest.getId());

        try {
            emailService.sendPurchaseRequestNotification(
                    product.getSeller().getEmail(),
                    buyer.getFullName(),
                    product.getTitle(),
                    dto.getOfferedPrice());
        } catch (Exception e) {
            log.error("Failed to send email notification: {}", e.getMessage());
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request has already been processed");
        }

        if (dto.getAccept()) {
            return handleAcceptRequest(request, dto.getResponseMessage());
        } else {
            return handleRejectRequest(request, dto.getRejectReason());
        }
    }

    private PurchaseRequestResponse handleAcceptRequest(PurchaseRequest request, String responseMessage) {
        Product product = request.getProduct();

        // Ẩn sản phẩm khi đồng ý bán
        product.setStatus(Product.Status.HIDDEN);
        productRepository.save(product);

        request.setSellerResponseMessage(responseMessage);
        request.setRespondedAt(LocalDateTime.now());
        request.setStatus(PurchaseRequest.RequestStatus.ACCEPTED);

        try {
            // Tạo hợp đồng trên Eversign (không cần ký, có điền Merge Fields)
            ContractInfoDTO contractInfo = eversignService.createContractWithoutSignature(
                    request.getBuyer(),
                    request.getSeller(),
                    product,
                    request.getOfferedPrice()
            );

            if (contractInfo != null && contractInfo.getContractId() != null) {
                request.setContractId(contractInfo.getContractId());
                request.setContractUrl(contractInfo.getContractUrl());
                request.setContractStatus(PurchaseRequest.ContractStatus.SENT);
                request.setStatus(PurchaseRequest.RequestStatus.CONTRACT_SENT);
            } else {
                log.warn("Eversign contract returned null or invalid response for request {}", request.getId());
                request.setContractStatus(PurchaseRequest.ContractStatus.CANCELLED);
            }

        } catch (Exception e) {
            log.error("Error creating contract for request {}: {}", request.getId(), e.getMessage());
            request.setContractStatus(PurchaseRequest.ContractStatus.CANCELLED);
        }

        PurchaseRequest savedRequest = purchaseRequestRepository.save(request);

        // Gửi email xác nhận cho buyer
        try {
            emailService.sendPurchaseAcceptedNotification(
                    request.getBuyer().getEmail(),
                    request.getSeller().getFullName(),
                    product.getTitle(),
                    savedRequest.getContractUrl()
            );
        } catch (Exception e) {
            log.error("Failed to send acceptance email: {}", e.getMessage());
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
                    rejectReason
            );
        } catch (Exception e) {
            log.error("Failed to send rejection email: {}", e.getMessage());
        }

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
    public PurchaseRequestResponse getRequestDetail(String id) {
        PurchaseRequest request = purchaseRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase request not found"));
        return mapToResponse(request);
    }

    @Transactional(readOnly = true)
    public long countPendingSellerRequests() {
        Account seller = userContextService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        return purchaseRequestRepository.countBySellerIdAndStatus(
                seller.getId(), PurchaseRequest.RequestStatus.PENDING);
    }

    private PurchaseRequestResponse mapToResponse(PurchaseRequest request) {
        Product product = request.getProduct();
        String thumbnail = (product.getImages() != null && !product.getImages().isEmpty())
                ? product.getImages().get(0).getImageUrl()
                : null;

        return PurchaseRequestResponse.builder()
                .id(request.getId())
                .productId(product.getId())
                .productTitle(product.getTitle())
                .productPrice(product.getPrice())
                .buyerId(request.getBuyer().getId())
                .buyerName(request.getBuyer().getFullName())
                .buyerEmail(request.getBuyer().getEmail())
                .sellerId(request.getSeller().getId())
                .sellerName(request.getSeller().getFullName())
                .sellerEmail(request.getSeller().getEmail())
                .offeredPrice(request.getOfferedPrice())
                .buyerMessage(request.getBuyerMessage())
                .sellerResponseMessage(request.getSellerResponseMessage())
                .status(request.getStatus().name())
                .contractId(request.getContractId())
                .contractUrl(request.getContractUrl())
                .rejectReason(request.getRejectReason())
                .createdAt(request.getCreatedAt())
                .respondedAt(request.getRespondedAt())
                .build();
    }
}