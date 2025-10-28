package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.transactions.*;
import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.entity.transactions.PurchaseRequest;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
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

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (product.getStatus() != Product.Status.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not available for purchase");
        }

        PurchaseRequest request = new PurchaseRequest();
        request.setId(UUID.randomUUID().toString());
        request.setProduct(product);
        request.setBuyer(buyer);
        request.setSeller(product.getSeller());
        request.setOfferedPrice(dto.getOfferedPrice() != null ? dto.getOfferedPrice() : product.getPrice());
        request.setBuyerMessage(dto.getBuyerMessage());
        request.setStatus(PurchaseRequest.RequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());

        PurchaseRequest saved = purchaseRequestRepository.save(request);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseRequestResponse> getBuyerRequests(Pageable pageable) {
        Account buyer = userContextService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        return purchaseRequestRepository.findByBuyerId(buyer.getId(), pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<PurchaseRequestResponse> getSellerRequests(Pageable pageable) {
        Account seller = userContextService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        return purchaseRequestRepository.findBySellerId(seller.getId(), pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public long countPendingSellerRequests() {
        Account seller = userContextService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        return purchaseRequestRepository.countBySellerIdAndStatus(seller.getId(), PurchaseRequest.RequestStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public PurchaseRequestResponse getRequestDetail(String requestId) {
        PurchaseRequest request = purchaseRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
        return mapToResponse(request);
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
        product.setStatus(Product.Status.HIDDEN);
        productRepository.save(product);

        request.setSellerResponseMessage(responseMessage);
        request.setRespondedAt(LocalDateTime.now());
        request.setStatus(PurchaseRequest.RequestStatus.ACCEPTED);
        request.setContractStatus(PurchaseRequest.ContractStatus.PENDING);

        try {
            ContractInfoDTO contractInfo = eversignService.createContractWithEmbeddedSigning(
                    request.getBuyer(),
                    request.getSeller(),
                    product,
                    request.getOfferedPrice()
            );

            if (contractInfo != null && contractInfo.getContractId() != null) {
                request.setContractId(contractInfo.getContractId());
                request.setContractUrl(contractInfo.getContractUrl());
                request.setBuyerSignUrl(contractInfo.getBuyerSignUrl());
                request.setSellerSignUrl(contractInfo.getSellerSignUrl());
                request.setContractStatus(PurchaseRequest.ContractStatus.SENT);
                request.setStatus(PurchaseRequest.RequestStatus.CONTRACT_SENT);

                PurchaseRequest savedRequest = purchaseRequestRepository.save(request);

                if (contractInfo.getBuyerSignUrl() != null) {
                    try {
                        emailService.sendContractToBuyer(
                                request.getBuyer().getEmail(),
                                request.getBuyer().getFullName(),
                                request.getSeller().getFullName(),
                                product.getTitle(),
                                contractInfo.getBuyerSignUrl()
                        );
                    } catch (Exception e) {
                        log.error("Failed to send email to buyer: {}", e.getMessage());
                    }
                }

                if (contractInfo.getSellerSignUrl() != null) {
                    try {
                        emailService.sendContractToSeller(
                                request.getSeller().getEmail(),
                                request.getSeller().getFullName(),
                                request.getBuyer().getFullName(),
                                product.getTitle(),
                                contractInfo.getSellerSignUrl()
                        );
                    } catch (Exception e) {
                        log.error("Failed to send email to seller: {}", e.getMessage());
                    }
                }

                return mapToResponse(savedRequest);

            } else {
                log.error("Eversign API returned OK but missing document_hash");
                throw new RuntimeException("Eversign API response missing document hash");
            }

        } catch (Exception e) {
            log.error("Error creating contract for request {}: {}", request.getId(), e.getMessage(), e);
            request.setContractStatus(PurchaseRequest.ContractStatus.FAILED);
            request.setContractId(null);
            request.setContractUrl(null);
            request.setStatus(PurchaseRequest.RequestStatus.CONTRACT_FAILED);
            purchaseRequestRepository.save(request);

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Đã chấp nhận yêu cầu nhưng không thể tạo hợp đồng điện tử. Vui lòng liên hệ support.");
        }
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

    private PurchaseRequestResponse mapToResponse(PurchaseRequest request) {
        Product product = request.getProduct();
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
                .contractId(request.getContractId())
                .contractUrl(request.getContractUrl())
                .rejectReason(request.getRejectReason())
                .createdAt(request.getCreatedAt())
                .respondedAt(request.getRespondedAt())
                .buyerSignedAt(request.getBuyerSignedAt())
                .sellerSignedAt(request.getSellerSignedAt())
                .build();
    }

    @Transactional
    public PurchaseRequestResponse acceptRequest(String requestId) {
        Account seller = userContextService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        PurchaseRequest request = purchaseRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        if (!request.getSeller().getId().equals(seller.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the seller of this product");
        }

        if (request.getStatus() != PurchaseRequest.RequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request has already been processed");
        }

        // Gọi hàm handleAcceptRequest
        return handleAcceptRequest(request, "Seller accepted the request");
    }

    @Transactional
    public PurchaseRequestResponse rejectRequest(String requestId, String reason) {
        Account seller = userContextService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        PurchaseRequest request = purchaseRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        if (!request.getSeller().getId().equals(seller.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the seller of this product");
        }

        if (request.getStatus() != PurchaseRequest.RequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request has already been processed");
        }

        // Gọi hàm handleRejectRequest
        return handleRejectRequest(request, reason != null ? reason : "Seller rejected the request");
    }

}
