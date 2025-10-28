// File: EversignService.java (không thay đổi gì, vì không có bug rõ ràng)
package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.transactions.ContractInfoDTO;
import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.entity.product.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service xử lý hợp đồng Eversign
 */
@Service
@Slf4j
public class EversignService {

    @Value("${EVERSIGN_TEMPLATE_ID}")
    private String templateId;

    @Value("${EVERSIGN_SANDBOX:true}")
    private boolean eversignSandbox;

    // TODO: Inject client Eversign nếu dùng SDK / HTTP client
    // private final EversignClient eversignClient;

    /**
     * Tạo hợp đồng từ template Eversign mà không yêu cầu chữ ký trực tiếp
     *
     * @param buyer        Người mua
     * @param seller       Người bán
     * @param product      Sản phẩm
     * @param offeredPrice Giá đề nghị
     * @return ContractInfoDTO chứa documentHash và embeddedSigningUrl
     */
    public ContractInfoDTO createContractWithoutSignature(Account buyer, Account seller, Product product, BigDecimal offeredPrice) {
        try {
            log.info("Creating Eversign contract using template: {}", templateId);

            // Khởi tạo document từ template
            Document templateDoc = new Document();
            templateDoc.setTemplateId(templateId);
            templateDoc.setSandbox(eversignSandbox);
            templateDoc.setTitle("Hợp đồng mua bán sản phẩm - " + product.getTitle());

            // Thêm signers
            List<Signer> signers = new ArrayList<>();

            Signer sellerSigner = new Signer();
            sellerSigner.setRole("Seller"); // Phải trùng với role trên template
            sellerSigner.setName(seller.getFullName());
            sellerSigner.setEmail(seller.getEmail());
            signers.add(sellerSigner);

            Signer buyerSigner = new Signer();
            buyerSigner.setRole("Buyer"); // Phải trùng với role trên template
            buyerSigner.setName(buyer.getFullName());
            buyerSigner.setEmail(buyer.getEmail());
            signers.add(buyerSigner);

            templateDoc.setSigners(signers);

            // Merge fields nếu template có placeholders
            Map<String, String> fields = Map.of(
                    "product_name", product.getTitle(),
                    "product_price", offeredPrice.toPlainString(),
                    "seller_name", seller.getFullName(),
                    "buyer_name", buyer.getFullName()
            );
            templateDoc.setCustomFields(fields);

            // Gọi API Eversign tạo document
            // Lưu ý: eversignClient.createDocumentFromTemplate() là giả lập. Bạn thay bằng client thật
            Document created = eversignClient.createDocumentFromTemplate(templateDoc);

            log.info("Eversign contract created successfully. Document hash: {}", created.getDocumentHash());

            return new ContractInfoDTO(created.getDocumentHash(), created.getEmbeddedSigningUrl());

        } catch (Exception e) {
            log.error("Error creating Eversign contract from template: {}", e.getMessage(), e);
            throw new RuntimeException("Eversign contract creation failed", e);
        }
    }

    // =======================
    // DTO & Helper Classes
    // =======================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Signer {
        private String role;                // Seller hoặc Buyer (phải trùng template)
        private String name;
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Document {
        private String templateId;                  // Template ID
        private String title;
        private boolean sandbox;
        private List<Signer> signers;
        private Map<String, String> customFields;
        private String documentHash;                // Kết quả từ Eversign
        private String embeddedSigningUrl;         // URL embedded signing
    }

    // TODO: Đây là placeholder client. Thay bằng SDK hoặc HTTP client thật
    private final FakeEversignClient eversignClient = new FakeEversignClient();

    private static class FakeEversignClient {
        public Document createDocumentFromTemplate(Document doc) {
            // Giả lập tạo contract trả về hash và embedded url
            doc.setDocumentHash("DOC-" + System.currentTimeMillis());
            doc.setEmbeddedSigningUrl("https://eversign.com/sign/" + doc.getDocumentHash());
            return doc;
        }
    }
}