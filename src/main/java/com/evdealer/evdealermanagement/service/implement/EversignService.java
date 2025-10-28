package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.transactions.ContractInfoDTO;
import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.entity.product.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
public class EversignService {

    @Value("${EVERSIGN_API_KEY}")
    private String apiKey;

    @Value("${EVERSIGN_BUSINESS_ID}")
    private String businessId;

    @Value("${EVERSIGN_TEMPLATE_ID}")
    private String templateId;

    @Value("${APP_BASE_URL:http://localhost:3000}")
    private String appBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String EVERSIGN_API_BASE = "https://api.eversign.com/api";

    public ContractInfoDTO createContractWithEmbeddedSigning(
            Account buyer,
            Account seller,
            Product product,
            BigDecimal offeredPrice) {

        try {
            log.info("Creating Eversign contract from template: {}", templateId);

            Map<String, Object> requestBody = buildContractRequest(buyer, seller, product, offeredPrice);

            String url = String.format("%s/document?business_id=%s&access_key=%s",
                    EVERSIGN_API_BASE, businessId, apiKey);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                String documentHash = (String) responseBody.get("document_hash");
                if (documentHash == null || documentHash.isEmpty()) {
                    throw new RuntimeException("Eversign API không trả về document_hash");
                }

                List<Map<String, Object>> signers = new ArrayList<>();
                Object signersObj = responseBody.get("signers");
                if (signersObj instanceof List) {
                    signers = (List<Map<String, Object>>) signersObj;
                }

                String buyerSignUrl = null;
                String sellerSignUrl = null;

                if (signers != null) {
                    for (Map<String, Object> signer : signers) {
                        String email = (String) signer.get("email");
                        String embeddedUrl = (String) signer.get("embedded_signing_url");

                        if (email != null && email.equalsIgnoreCase(buyer.getEmail())) {
                            buyerSignUrl = embeddedUrl;
                        } else if (email != null && email.equalsIgnoreCase(seller.getEmail())) {
                            sellerSignUrl = embeddedUrl;
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

            } else {
                throw new RuntimeException("Eversign API error: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Error creating Eversign contract: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create contract: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> buildContractRequest(
            Account buyer,
            Account seller,
            Product product,
            BigDecimal offeredPrice) {

        Map<String, Object> request = new HashMap<>();
        request.put("sandbox", 1);
        request.put("template_id", templateId);
        request.put("title", "Hợp đồng mua bán - " + product.getTitle());
        request.put("embedded_signing_enabled", 1);
        request.put("client", appBaseUrl);
        request.put("redirect", appBaseUrl + "/contract/completed");
        request.put("redirect_decline", appBaseUrl + "/contract/declined");

        List<Map<String, Object>> signers = new ArrayList<>();

        Map<String, Object> sellerSigner = new HashMap<>();
        sellerSigner.put("role", "seller");
        sellerSigner.put("name", seller.getFullName());
        sellerSigner.put("email", seller.getEmail());
        sellerSigner.put("order", 1);
        sellerSigner.put("deliver_email", 0);
        signers.add(sellerSigner);

        Map<String, Object> buyerSigner = new HashMap<>();
        buyerSigner.put("role", "buyer");
        buyerSigner.put("name", buyer.getFullName());
        buyerSigner.put("email", buyer.getEmail());
        buyerSigner.put("order", 2);
        buyerSigner.put("deliver_email", 0);
        signers.add(buyerSigner);

        request.put("signers", signers);

        List<Map<String, String>> fields = new ArrayList<>();
        fields.add(createField("product_name", product.getTitle()));
        fields.add(createField("product_price", formatCurrency(offeredPrice)));
        fields.add(createField("product_total", formatCurrency(offeredPrice)));
        fields.add(createField("seller_name", seller.getFullName()));
        fields.add(createField("seller_email", seller.getEmail()));
        fields.add(createField("seller_phone", seller.getPhone() != null ? seller.getPhone() : "N/A"));
        fields.add(createField("buyer_name", buyer.getFullName()));
        fields.add(createField("buyer_email", buyer.getEmail()));
        fields.add(createField("buyer_phone", buyer.getPhone() != null ? buyer.getPhone() : "N/A"));
        request.put("fields", fields);

        return request;
    }

    private Map<String, String> createField(String identifier, String value) {
        Map<String, String> field = new HashMap<>();
        field.put("identifier", identifier);
        field.put("value", value);
        return field;
    }

    private String buildContractViewUrl(String documentHash) {
        return String.format("https://eversign.com/documents/%s", documentHash);
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0 VND";
        }
        return String.format("%,.0f VND", amount);
    }
}
