package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.transactions.CreatePurchaseRequestDTO;
import com.evdealer.evdealermanagement.dto.transactions.PurchaseRequestResponse;
import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.entity.transactions.PurchaseRequest;
import com.evdealer.evdealermanagement.exceptions.AppException;
import com.evdealer.evdealermanagement.exceptions.ErrorCode;
import com.evdealer.evdealermanagement.repository.ProductRepository;
import com.evdealer.evdealermanagement.repository.PurchaseRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /** Tạo Purchase Request */
    @Transactional
    public PurchaseRequestResponse createPurchaseRequest(CreatePurchaseRequestDTO dto) {
        Account buyer = userContextService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

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
                .build();

        PurchaseRequest savedRequest = purchaseRequestRepository.save(request);

        emailService.sendPurchaseRequestNotification(
                product.getSeller().getEmail(),
                buyer.getFullName(),
                product.getTitle(),
                dto.getOfferedPrice());

        log.info("Purchase request created. ID: {}, Product: {}, Buyer: {}",
                savedRequest.getId(), product.getId(), buyer.getId());

        return mapToResponse(savedRequest);
    }

    /** Seller chấp nhận request */
    @Transactional
    public PurchaseRequestResponse acceptPurchaseRequest(String requestId) {
        Account seller = userContextService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        PurchaseRequest request = purchaseRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.PURCHASE_REQUEST_NOT_FOUND));

        if (!request.getSeller().getId().equals(seller.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your purchase request");
        }

        request.setStatus(PurchaseRequest.RequestStatus.ACCEPTED);
        request.setRespondedAt(LocalDateTime.now());

        // Tạo contract trên Eversign
        var contractInfo = eversignService.createAndSendContract(
                request.getBuyer(),
                request.getSeller(),
                request.getProduct(),
                request.getOfferedPrice());

        request.setContractId(contractInfo.getContractId());
        request.setContractUrl(contractInfo.getContractUrl());
        request.setContractStatus(PurchaseRequest.ContractStatus.SENT);

        PurchaseRequest saved = purchaseRequestRepository.save(request);

        // Gửi email cho buyer
        emailService.sendPurchaseAcceptedNotification(
                saved.getBuyer().getEmail(),
                seller.getFullName(),
                saved.getProduct().getTitle(),
                saved.getContractUrl());

        log.info("Purchase request accepted. ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    /** Seller từ chối request */
    @Transactional
    public PurchaseRequestResponse rejectPurchaseRequest(String requestId, String reason) {
        Account seller = userContextService.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        PurchaseRequest request = purchaseRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.PURCHASE_REQUEST_NOT_FOUND));

        if (!request.getSeller().getId().equals(seller.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your purchase request");
        }

        request.setStatus(PurchaseRequest.RequestStatus.REJECTED);
        request.setRejectReason(reason);
        request.setRespondedAt(LocalDateTime.now());

        PurchaseRequest saved = purchaseRequestRepository.save(request);

        // Gửi email cho buyer
        emailService.sendPurchaseRejectedNotification(
                saved.getBuyer().getEmail(),
                seller.getFullName(),
                saved.getProduct().getTitle(),
                reason);

        log.info("Purchase request rejected. ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    /** Cập nhật trạng thái hợp đồng khi buyer/seller ký xong */
    @Transactional
    public PurchaseRequestResponse completeContract(String requestId, boolean isBuyerSigned, boolean isSellerSigned) {
        PurchaseRequest request = purchaseRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.PURCHASE_REQUEST_NOT_FOUND));

        if (isBuyerSigned) {
            request.setBuyerSignedAt(LocalDateTime.now());
        }
        if (isSellerSigned) {
            request.setSellerSignedAt(LocalDateTime.now());
        }

        if (request.getBuyerSignedAt() != null && request.getSellerSignedAt() != null) {
            request.setContractStatus(PurchaseRequest.ContractStatus.COMPLETED);
            request.setStatus(PurchaseRequest.RequestStatus.COMPLETED);

            Product product = request.getProduct();
            product.setStatus(Product.Status.SOLD);
            productRepository.save(product);

            // Gửi email thông báo hoàn tất cho buyer & seller
            emailService.sendContractCompletedNotification(
                    request.getBuyer().getEmail(),
                    request.getSeller().getEmail(),
                    request.getProduct().getTitle());
        }

        PurchaseRequest saved = purchaseRequestRepository.save(request);
        log.info("Contract updated. ID: {}, BuyerSigned: {}, SellerSigned: {}",
                saved.getId(), saved.getBuyerSignedAt(), saved.getSellerSignedAt());

        return mapToResponse(saved);
    }

    /** Mapper entity -> DTO */
    private PurchaseRequestResponse mapToResponse(PurchaseRequest request) {
        return PurchaseRequestResponse.builder()
                .id(request.getId())
                .productId(request.getProduct().getId())
                .buyerId(request.getBuyer().getId())
                .sellerId(request.getSeller().getId())
                .offeredPrice(request.getOfferedPrice())
                .status(request.getStatus().name())
                .contractStatus(request.getContractStatus() != null ? request.getContractStatus().name() : null)
                .contractId(request.getContractId())
                .contractUrl(request.getContractUrl())
                .buyerSignedAt(request.getBuyerSignedAt())
                .sellerSignedAt(request.getSellerSignedAt())
                .buyerMessage(request.getBuyerMessage())
                .sellerResponseMessage(request.getSellerResponseMessage())
                .rejectReason(request.getRejectReason())
                .createdAt(request.getCreatedAt())
                .respondedAt(request.getRespondedAt())
                .build();
    }
}
