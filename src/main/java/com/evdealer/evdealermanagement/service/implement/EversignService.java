package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.transactions.ContractInfoDTO;
import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.entity.product.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class EversignService {

    private final Dotenv dotenv;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String accessKey;
    private String businessId;
    private String webhookUrl;
    private String contractTemplateUrl;
    private boolean useSandbox;

    @PostConstruct
    public void init() {
        this.accessKey = dotenv.get("EVERSIGN_ACCESS_KEY");
        this.businessId = dotenv.get("EVERSIGN_BUSINESS_ID");
        this.webhookUrl = dotenv.get("EVERSIGN_WEBHOOK_URL");
        this.contractTemplateUrl = dotenv.get("EVERSIGN_CONTRACT_TEMPLATE_URL",
                "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf");
        this.useSandbox = Boolean.parseBoolean(dotenv.get("EVERSIGN_SANDBOX", "true"));

        log.info("=== EVERSIGN SERVICE INITIALIZED ===");
        log.info("Business ID: {}", businessId);
        log.info("Webhook URL: {}", webhookUrl);
        log.info("Sandbox Mode: {}", useSandbox);

        if (accessKey == null || accessKey.isEmpty()) {
            log.error("EVERSIGN_ACCESS_KEY not found!");
            throw new IllegalStateException("EVERSIGN_ACCESS_KEY is required");
        }
    }

    public ContractInfoDTO createAndSendContract(
            Account buyer,
            Account seller,
            Product product,
            BigDecimal agreedPrice) {

        try {
            log.info("📄 Creating contract for Product: {}, Buyer: {}, Seller: {}",
                    product.getId(), buyer.getId(), seller.getId());

            Map<String, Object> contractData = buildContractData(buyer, seller, product, agreedPrice);

            String url = String.format(
                    "https://api.eversign.com/api/document?access_key=%s&business_id=%s&sandbox=%d",
                    accessKey, businessId, useSandbox ? 1 : 0);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(contractData, headers);

            log.info("📤 Sending contract request to Eversign...");
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                String documentHash = (String) responseBody.get("document_hash");

                log.info("✅ Contract created successfully!");
                log.info("📋 Document Hash: {}", documentHash);

                String contractViewUrl = String.format(
                        "https://eversign.com/document/%s", documentHash);

                return ContractInfoDTO.builder()
                        .contractId(documentHash)
                        .contractUrl(contractViewUrl)
                        .buyerSignUrl(contractViewUrl)
                        .sellerSignUrl(contractViewUrl)
                        .status("SENT")
                        .build();

            } else {
                log.error("❌ Failed to create contract. Status: {}", response.getStatusCode());
                throw new RuntimeException("Failed to create contract: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ Error creating Eversign contract: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create contract: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> buildContractData(
            Account buyer,
            Account seller,
            Product product,
            BigDecimal agreedPrice) {

        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String formattedPrice = formatCurrency(agreedPrice);

        Map<String, Object> data = new HashMap<>();

        data.put("type", "signature_request");
        data.put("title", String.format("Hợp đồng mua bán - %s", product.getTitle()));
        data.put("message", String.format(
                "Hợp đồng mua bán sản phẩm: %s\n" +
                        "Giá thỏa thuận: %s VNĐ\n" +
                        "Ngày tạo: %s\n\n" +
                        "Vui lòng xem xét và ký hợp đồng.",
                product.getTitle(), formattedPrice, currentDate));

        data.put("sandbox", useSandbox ? 1 : 0);
        data.put("use_signer_order", 1);
        data.put("embedded_signing_enabled", 0);
        data.put("reminders", 1);
        data.put("require_all_signers", 1);

        List<Map<String, Object>> signers = new ArrayList<>();

        Map<String, Object> sellerSigner = new HashMap<>();
        sellerSigner.put("id", 1);
        sellerSigner.put("name", seller.getFullName());
        sellerSigner.put("email", seller.getEmail());
        sellerSigner.put("order", 1);
        sellerSigner.put("role", "seller");
        sellerSigner.put("message", "Vui lòng ký để xác nhận bán sản phẩm");
        signers.add(sellerSigner);

        Map<String, Object> buyerSigner = new HashMap<>();
        buyerSigner.put("id", 2);
        buyerSigner.put("name", buyer.getFullName());
        buyerSigner.put("email", buyer.getEmail());
        buyerSigner.put("order", 2);
        buyerSigner.put("role", "buyer");
        buyerSigner.put("message", "Vui lòng ký để xác nhận mua sản phẩm");
        signers.add(buyerSigner);

        data.put("signers", signers);

        List<Map<String, Object>> files = new ArrayList<>();
        Map<String, Object> file = new HashMap<>();
        file.put("name", String.format("hop_dong_%s.pdf", product.getId()));
        file.put("file_url", contractTemplateUrl);
        files.add(file);

        data.put("files", files);

        List<Map<String, String>> meta = new ArrayList<>();
        meta.add(Map.of("product_id", product.getId()));
        meta.add(Map.of("buyer_id", buyer.getId()));
        meta.add(Map.of("seller_id", seller.getId()));
        meta.add(Map.of("price", agreedPrice.toString()));
        data.put("meta", meta);

        return data;
    }

    private String formatCurrency(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }
}