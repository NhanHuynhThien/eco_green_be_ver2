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

        log.info("EversignService initialized: businessId={}, sandbox={}", businessId, useSandbox);

        if (accessKey == null || accessKey.isEmpty()) {
            throw new IllegalStateException("EVERSIGN_ACCESS_KEY is required");
        }
    }

    /**
     * Primary method to create a document on Eversign and send it.
     * This method builds request body, posts to Eversign API and extracts document_hash.
     */
    public ContractInfoDTO createAndSendContract(
            Account buyer,
            Account seller,
            Product product,
            BigDecimal agreedPrice,
            // THAM SỐ MỚI để kiểm soát việc Eversign có gửi email yêu cầu ký hay không
            boolean sendEmailByEversign) {

        try {
            log.info("Creating contract for product={}, buyer={}, seller={}, sendEmailByEversign={}",
                    product.getId(), buyer.getId(), seller.getId(), sendEmailByEversign);

            Map<String, Object> contractData = buildContractData(buyer, seller, product, agreedPrice, sendEmailByEversign);

            String url = String.format(
                    "https://api.eversign.com/api/document?access_key=%s&business_id=%s&sandbox=%d",
                    accessKey, businessId, useSandbox ? 1 : 0);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(contractData, headers);

            log.info("Sending request to Eversign API: {}", url);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                log.error("Eversign API returned non-OK or empty body: {}", response.getStatusCode());
                throw new RuntimeException("Eversign API returned invalid response: " + response.getStatusCode());
            }

            Map<String, Object> responseBody = response.getBody();
            String documentHash = extractDocumentHash(responseBody);

            if (documentHash == null) {
                log.error("document_hash not found in Eversign response: {}", objectMapper.writeValueAsString(responseBody));
                throw new RuntimeException("Missing document_hash in Eversign response");
            }

            String contractViewUrl = String.format("https://eversign.com/document/%s", documentHash);
            String embeddedSignUrl = String.format("https://eversign.com/api/document/sign?document_hash=%s", documentHash);

            log.info("Eversign document created: document_hash={}", documentHash);

            return ContractInfoDTO.builder()
                    .contractId(documentHash)
                    .contractUrl(contractViewUrl)
                    .buyerSignUrl(embeddedSignUrl)
                    .sellerSignUrl(embeddedSignUrl)
                    .status("SENT")
                    .build();

        } catch (Exception e) {
            log.error("Error creating Eversign contract: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create contract: " + e.getMessage(), e);
        }
    }

    /**
     * Convenience method required by service logic:
     * create contract on Eversign but do not require further signing steps from application side.
     */
    public ContractInfoDTO createContractWithoutSignature(
            Account buyer,
            Account seller,
            Product product,
            BigDecimal agreedPrice) {
        // Tắt tính năng tự động gửi email yêu cầu ký của Eversign (sendEmailByEversign = false)
        return createAndSendContract(buyer, seller, product, agreedPrice, false);
    }

    /**
     * Extract document_hash from various possible shapes of Eversign response.
     */
    private String extractDocumentHash(Map<String, Object> body) {
        try {
            if (body.containsKey("document_hash") && body.get("document_hash") instanceof String) {
                return (String) body.get("document_hash");
            }

            if (body.containsKey("document") && body.get("document") instanceof Map<?, ?> doc) {
                Object v = doc.get("document_hash");
                if (v instanceof String) return (String) v;
            }

            if (body.containsKey("documents") && body.get("documents") instanceof List<?> docs && !docs.isEmpty()) {
                Object first = docs.get(0);
                if (first instanceof Map<?, ?> m) {
                    Object v = m.get("document_hash");
                    if (v instanceof String) return (String) v;
                }
            }
        } catch (Exception e) {
            log.error("Error extracting document_hash from Eversign response: {}", e.getMessage(), e);
        }
        return null;
    }

    // =========================================================================
    // CẬP NHẬT: buildContractData để thêm Title trang trọng và trường Fields
    // =========================================================================
    /**
     * Build JSON body sent to Eversign API.
     */
    private Map<String, Object> buildContractData(
            Account buyer,
            Account seller,
            Product product,
            BigDecimal agreedPrice,
            boolean sendEmailByEversign) {

        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String formattedPrice = formatCurrency(agreedPrice);

        // 1. Cải tiến Title Hợp đồng cho trang trọng
        String contractTitle = String.format("HỢP ĐỒNG MUA BÁN Ô TÔ ĐIỆN - %s", product.getTitle());

        Map<String, Object> data = new HashMap<>();
        data.put("type", "signature_request");
        data.put("title", contractTitle); // Dùng tiêu đề mới
        data.put("message", String.format("Hợp đồng mua bán sản phẩm: %s\nGiá thỏa thuận: %s VNĐ\nNgày tạo: %s",
                product.getTitle(), formattedPrice, currentDate));
        data.put("sandbox", useSandbox ? 1 : 0);
        data.put("use_signer_order", 1);
        data.put("reminders", 1);
        data.put("require_all_signers", 1);

        // ĐIỀU CHỈNH QUAN TRỌNG: Ngăn Eversign tự động gửi email yêu cầu ký
        data.put("client_only", sendEmailByEversign ? 0 : 1);

        // 2. THÊM TRƯỜNG 'fields' ĐỂ CHÈN DỮ LIỆU VÀO HỢP ĐỒNG PDF
        // LƯU Ý: Đảm bảo template PDF của bạn có các field placeholder với tên tương ứng
        List<Map<String, Object>> fields = new ArrayList<>();

        // Thông tin Sản phẩm
        fields.add(createField("text", "product_title", product.getTitle())); // Tên Field trong PDF: product_title
        fields.add(createField("text", "product_description", product.getDescription())); // Tên Field trong PDF: product_description
        fields.add(createField("text", "product_year", String.valueOf(product.getManufactureYear()))); // Tên Field trong PDF: product_year

        // Thông tin Giao dịch
        fields.add(createField("text", "agreed_price", formattedPrice)); // Tên Field trong PDF: agreed_price
        fields.add(createField("text", "agreed_price_text", "Bằng chữ: " + /* Thêm logic chuyển đổi số sang chữ nếu cần */ formattedPrice));
        fields.add(createField("text", "contract_date", currentDate)); // Tên Field trong PDF: contract_date

        // Thông tin Bên mua và Bên bán
        fields.add(createField("text", "buyer_name", buyer.getFullName())); // Tên Field trong PDF: buyer_name
        fields.add(createField("text", "seller_name", seller.getFullName())); // Tên Field trong PDF: seller_name

        data.put("fields", fields);


        List<Map<String, Object>> signers = new ArrayList<>();
        Map<String, Object> sellerSigner = new HashMap<>();
        sellerSigner.put("id", 1);
        sellerSigner.put("name", seller.getFullName());
        sellerSigner.put("email", seller.getEmail());
        sellerSigner.put("order", 1);
        sellerSigner.put("role", "seller");
        sellerSigner.put("message", "Please sign to confirm selling the product.");
        signers.add(sellerSigner);

        Map<String, Object> buyerSigner = new HashMap<>();
        buyerSigner.put("id", 2);
        buyerSigner.put("name", buyer.getFullName());
        buyerSigner.put("email", buyer.getEmail());
        buyerSigner.put("order", 2);
        buyerSigner.put("role", "buyer");
        buyerSigner.put("message", "Please sign to confirm buying the product.");
        signers.add(buyerSigner);

        data.put("signers", signers);

        Map<String, Object> file = new HashMap<>();
        file.put("name", "hop_dong_" + product.getId() + ".pdf");
        file.put("file_url", contractTemplateUrl);
        data.put("files", List.of(file));

        Map<String, String> meta = new LinkedHashMap<>();
        meta.put("product_id", product.getId());
        meta.put("buyer_id", buyer.getId());
        meta.put("seller_id", seller.getId());
        meta.put("price", agreedPrice.toString());
        data.put("meta", meta);

        return data;
    }

    // =========================================================================
    // Phương thức tiện ích để tạo Field Map
    // =========================================================================
    /**
     * Helper method to create a field map for Eversign API.
     * @param type The type of the field (e.g., 'text', 'checkbox').
     * @param name The name of the field in the PDF template (the placeholder name).
     * @param value The value to be inserted into the field.
     */
    private Map<String, Object> createField(String type, String name, String value) {
        Map<String, Object> field = new HashMap<>();
        field.put("type", type);
        field.put("name", name);
        field.put("value", value);
        // Field needs to be associated with the first file in the files array (index 0)
        field.put("file_index", 0);
        return field;
    }
    // =========================================================================

    private String formatCurrency(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }
}