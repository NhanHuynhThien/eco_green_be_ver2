package com.evdealer.evdealermanagement.service.implement;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.evdealer.evdealermanagement.dto.transactions.ContractInfoDTO;
import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.evdealer.evdealermanagement.entity.transactions.ContractDocument;
import com.evdealer.evdealermanagement.entity.transactions.PurchaseRequest;
import com.evdealer.evdealermanagement.exceptions.AppException;
import com.evdealer.evdealermanagement.exceptions.ErrorCode;
import com.evdealer.evdealermanagement.repository.ContractDocumentRepository;
import com.evdealer.evdealermanagement.utils.VietNamDatetime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class EversignService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ContractDocumentRepository contractDocumentRepository;
    private final EmailService emailService;

    // CLoudinary config
    @Value("${CLOUDINARY_CLOUD_NAME}")
    private String cloudName;

    @Value("${CLOUDINARY_API_KEY}")
    private String cloudApiKey;

    @Value("${CLOUDINARY_API_SECRET}")
    private String cloudApiSecret;

    // Eversign Config
    @Value("${EVERSIGN_API_KEY}")
    private String apiKey;

    @Value("${EVERSIGN_BUSINESS_ID}")
    private String businessId;

    @Value("${EVERSIGN_TEMPLATE_ID}")
    private String templateId;

    @Value("${EVERSIGN_SANDBOX:true}") // ✅ mặc định sandbox
    private boolean sandboxMode;

    @Value("${APP_BASE_URL:http://localhost:3000}")
    private String appBaseUrl;

    private static final String EVERSIGN_API_BASE = "https://api.eversign.com/api";

    /**
     * Tạo hợp đồng để hai bên tự điền và ký (sandbox mode)
     */
    public ContractInfoDTO createBlankContractForManualInput(
            Account buyer,
            Account seller,
            Product product
    ) {
        try {
            log.info("🚀 [Eversign] Tạo hợp đồng trống (sandboxMode={})", sandboxMode);

            Map<String, Object> requestBody = buildContractRequest(buyer, seller, product);

            String url = String.format("%s/document?business_id=%s&access_key=%s",
                    EVERSIGN_API_BASE, businessId, apiKey);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            log.info("📬 [Eversign] Response status: {}", response.getStatusCode());
            log.debug("📥 [Eversign] Full response: {}", response.getBody());

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new AppException(ErrorCode.CONTRACT_BUILD_FAILED);
            }

            Map<String, Object> body = response.getBody();
            String documentHash = (String) body.get("document_hash");
            if (documentHash == null) {
                throw new AppException(ErrorCode.CONTRACT_BUILD_FAILED);
            }

            // Tạo link ký
            String buyerSignUrl = null;
            String sellerSignUrl = null;

            Object signersObj = body.get("signers");
            if (signersObj instanceof List<?> signersList) {
                for (Object obj : signersList) {
                    if (obj instanceof Map<?, ?> signer) {
                        String email = (String) signer.get("email");
                        String embeddedUrl = (String) signer.get("embedded_signing_url");
                        if (email != null && email.equalsIgnoreCase(buyer.getEmail())) {
                            buyerSignUrl = embeddedUrl;
                        } else if (email != null && email.equalsIgnoreCase(seller.getEmail())) {
                            sellerSignUrl = embeddedUrl;
                        }
                    }
                }
            }

            return ContractInfoDTO.builder()
                    .contractId(documentHash)
                    .contractUrl(buildContractViewUrl(documentHash))
                    .buyerSignUrl(buyerSignUrl)
                    .sellerSignUrl(sellerSignUrl)
                    .status("PENDING")
                    .build();

        } catch (Exception e) {
            log.error("🔥 [Eversign] Lỗi khi tạo hợp đồng trống: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi tạo hợp đồng với Eversign: " + e.getMessage());
        }
    }

    private Map<String, Object> buildContractRequest(
            Account buyer,
            Account seller,
            Product product
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put("sandbox", sandboxMode ? 1 : 0); // ✅ bật sandbox
        body.put("business_id", businessId);
        body.put("template_id", templateId);
        body.put("title", "Hợp đồng mua bán xe điện (sandbox)");
        body.put("message", "Vui lòng điền thông tin và ký hợp đồng (sandbox).");
//        body.put("embedded_signing_enabled", 1);
//        body.put("use_signer_order", 1);
//        body.put("redirect", appBaseUrl + "/contract/completed");
//        body.put("redirect_decline", appBaseUrl + "/contract/declined");

        // 👥 Người ký
        List<Map<String, Object>> signers = new ArrayList<>();
        signers.add(Map.of(
                "role", "seller",
                "name", seller.getFullName(),
                "email", seller.getEmail(),
                "signing_order", 1
        ));
        signers.add(Map.of(
                "role", "buyer",
                "name", buyer.getFullName(),
                "email", buyer.getEmail(),
                "signing_order", 2
        ));
        body.put("signers", signers);

        log.debug("🧰 [Eversign] Request body (sandbox={}): {}", sandboxMode, body);
        return body;
    }

    private String buildContractViewUrl(String documentHash) {
        return String.format("https://eversign.com/documents/%s", documentHash);
    }

    // Download pdf, upload Cloudinary
    public void saveContractToDatabase(PurchaseRequest request) {
        try {
            String documentHash = request.getContractId();
            log.info("📑 [Eversign] Lưu hợp đồng PDF vào DB cho staff/admin, documentHash={}", documentHash);

            String pdfUrl = String.format(
                    "https://api.eversign.com/api/document/download_original?business_id=%s&access_key=%s&document_hash=%s",
                    businessId, apiKey, documentHash
            );

            File tempFile = Files.createTempFile("contract_", ".pdf").toFile();
            try (InputStream in = new URL(pdfUrl).openStream();
                 OutputStream out = new FileOutputStream(tempFile)) {
                in.transferTo(out);
            }

            // Up len Cloudinary
            Cloudinary cloudinary = new Cloudinary(
                    ObjectUtils.asMap(
                            "cloud_name", cloudName,
                            "api_key", cloudApiKey,
                            "api_secret", cloudApiSecret
                    )
            );

            Map uploadResult = cloudinary.uploader().upload(
                    tempFile,
                    ObjectUtils.asMap(
                            "folder", "contracts",
                            "resource_type", "raw"
                    )
            );

            String cloudUrl = (String) uploadResult.get("secure_url");

            // Luu vao DB
            ContractDocument contract = new ContractDocument();
            contract.setDocumentId(documentHash);
            contract.setTitle("Hợp đồng mua bán - " + request.getProduct().getTitle());
            contract.setPdfUrl(cloudUrl);
            contract.setSignerEmail(request.getBuyer().getEmail());
            contract.setSignedAt(VietNamDatetime.nowVietNam());

            contractDocumentRepository.save(contract);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
