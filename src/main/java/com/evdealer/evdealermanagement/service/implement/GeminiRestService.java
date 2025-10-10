package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.price.PriceSuggestion;
import com.evdealer.evdealermanagement.utils.PriceSerializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service để tích hợp Google Gemini API cho việc gợi ý giá sản phẩm
 * Sử dụng REST API trực tiếp thông qua RestTemplate
 */
@Service
@Slf4j
public class GeminiRestService {

    private final Dotenv dotenv;

    private String apiKey;
    private String modelName;
    private int maxTokens;
    private float temperature;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiRestService(Dotenv dotenv) {
        this.dotenv = dotenv;
    }

    @PostConstruct
    public void init() {
        this.apiKey = dotenv.get("GEMINI_API_KEY");
        this.modelName = dotenv.get("GEMINI_MODEL", "gemini-1.5-flash");
        this.maxTokens = Integer.parseInt(dotenv.get("GEMINI_MAX_TOKENS", "500"));
        this.temperature = Float.parseFloat(dotenv.get("GEMINI_TEMPERATURE", "0.2"));

        log.info("=== GEMINI REST SERVICE INITIALIZED ===");
        log.info("Model: {}", modelName);
        log.info("MaxTokens: {}", maxTokens);
        log.info("Temperature: {}", temperature);

        if (apiKey == null || apiKey.isEmpty()) {
            log.error("❌ GEMINI_API_KEY not found in .env file!");
            throw new IllegalStateException("GEMINI_API_KEY is required");
        }
    }

    /**
     * Gợi ý giá cho sản phẩm dựa trên tiêu đề
     * @param title Tiêu đề sản phẩm
     * @return PriceSuggestion chứa giá và mô tả
     */
    public PriceSuggestion suggestPrice(String title) {
        String prompt = buildPrompt(title);

        try {
            log.info("=== GEMINI REST API REQUEST ===");
            log.info("Title: {}", title);

            String url = String.format(
                    "https://generativelanguage.googleapis.com/v1/models/%s:generateContent?key=%s",
                    modelName, apiKey
            );

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", prompt)))
                    ),
                    "generationConfig", Map.of(
                            "temperature", temperature,
                            "maxOutputTokens", maxTokens,
                            "topP", 0.9,
                            "topK", 40
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            log.info("Response status: {}", response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return handleSuccessResponse(response.getBody(), title);
            } else {
                log.error("❌ Unexpected response status: {}", response.getStatusCode());
                return generateFallback(title);
            }

        } catch (Exception e) {
            log.error("❌ Error calling Gemini REST API: {}", e.getMessage(), e);
            return generateFallback(title);
        }
    }

    /**
     * Xây dựng prompt cho Gemini
     */
    private String buildPrompt(String title) {
        return String.format(
                "Bạn là chuyên gia thẩm định giá các sản phẩm CŨ tại Việt Nam (xe điện, pin, linh kiện điện, thiết bị điện tử, v.v.).\n" +
                        "Nhiệm vụ: Đưa ra **giá gợi ý hợp lý** và **mô tả ngắn gọn về tình trạng sản phẩm** dựa trên thông tin người bán cung cấp.\n\n" +
                        "Sản phẩm: %s\n\n" +
                        "Yêu cầu phản hồi:\n" +
                        "1️⃣ Chỉ trả về đúng định dạng sau (không markdown, không ký tự đặc biệt):\n" +
                        "Giá gợi ý: [khoảng giá hoặc số cụ thể] VNĐ\n" +
                        "Mô tả ngắn gọn: [1 câu đánh giá tổng quan tình trạng, độ mới, hiệu suất hoặc giá trị sử dụng]\n\n" +
                        "Ví dụ:\n" +
                        "Giá gợi ý: Khoảng 58–62 triệu VNĐ\n" +
                        "Mô tả ngắn gọn: Pin xe điện còn 90%%, giữ hiệu suất tốt, thích hợp để tái sử dụng.\n\n" +
                        "Hoặc:\n" +
                        "Giá gợi ý: 12.500.000 VNĐ\n" +
                        "Mô tả ngắn gọn: Bộ sạc xe điện hoạt động ổn định, bề ngoài hơi trầy nhưng hiệu năng tốt.\n\n" +
                        "Chỉ trả về đúng format trên, không thêm lời giải thích.",
                title
        );
    }

    /**
     * Xử lý response thành công từ Gemini API
     */
    private PriceSuggestion handleSuccessResponse(String responseBody, String title) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            // Kiểm tra lỗi trong response
            if (root.has("error")) {
                JsonNode error = root.get("error");
                log.error("API Error: {} - {}",
                        error.path("code").asInt(),
                        error.path("message").asText()
                );
                return generateFallback(title);
            }

            // Lấy text từ response
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode textNode = candidates.get(0)
                        .path("content")
                        .path("parts")
                        .get(0)
                        .path("text");

                if (textNode != null && !textNode.asText().isEmpty()) {
                    String text = textNode.asText();
                    log.info("✅ Gemini response received");
                    log.debug("Raw response: {}", text);
                    return parseResponse(text, title);
                }
            }

            log.error("❌ Unexpected response format: no text content found");
            return generateFallback(title);

        } catch (Exception e) {
            log.error("❌ Error parsing Gemini response: {}", e.getMessage(), e);
            return generateFallback(title);
        }
    }

    /**
     * Parse response text từ Gemini thành PriceSuggestion
     */
    private PriceSuggestion parseResponse(String rawText, String title) {
        // Pattern để match "Giá gợi ý: ... VNĐ"
        Pattern pricePattern = Pattern.compile(
                "Giá gợi ý:\\s*(.+?VNĐ)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        // Pattern để match "Mô tả ngắn gọn: ..."
        Pattern reasonPattern = Pattern.compile(
                "Mô tả ngắn gọn:\\s*(.+?)(?=\\n|$)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        Matcher priceMatcher = pricePattern.matcher(rawText);
        Matcher reasonMatcher = reasonPattern.matcher(rawText);

        String priceStr = priceMatcher.find()
                ? priceMatcher.group(1).trim()
                : "Không xác định được giá";

        String reason = reasonMatcher.find()
                ? reasonMatcher.group(1).trim()
                : "Chưa có mô tả chi tiết.";

        // Clean up markdown và ký tự không mong muốn
        priceStr = cleanText(priceStr);
        reason = cleanText(reason);

        log.info("✅ Parsed → Price: {}, Reason: {}", priceStr, reason);
        return new PriceSuggestion(priceStr, reason);
    }

    /**
     * Làm sạch text: bỏ markdown, ký tự đặc biệt
     */
    private String cleanText(String text) {
        return text
                .replaceAll("\\*+", "")           // Bỏ dấu *
                .replaceAll("\\n+", " ")          // Thay newline bằng space
                .replaceAll("\\s{2,}", " ")       // Bỏ multiple spaces
                .trim();
    }

    /**
     * Generate fallback price khi API fail hoặc không có response
     */
    private PriceSuggestion generateFallback(String title) {
        log.warn("⚠️ Using fallback pricing for: {}", title);

        String lowerTitle = title.toLowerCase();

        // Giá base theo loại sản phẩm
        BigDecimal basePrice = determineBasePrice(lowerTitle);

        // Factor điều chỉnh theo tình trạng
        BigDecimal factor = determinePriceFactor(lowerTitle);

        // Tính giá ước tính
        BigDecimal estimated = basePrice
                .multiply(factor)
                .setScale(0, RoundingMode.HALF_UP);

        String formatted = PriceSerializer.formatPrice(estimated);

        String reason = String.format(
                "Giá ước tính tham khảo cho '%s' (tình trạng khoảng %.0f%% giá gốc).",
                title,
                factor.multiply(BigDecimal.valueOf(100)).doubleValue()
        );

        return new PriceSuggestion("Khoảng " + formatted + " VNĐ", reason);
    }

    /**
     * Xác định giá base theo loại sản phẩm
     */
    private BigDecimal determineBasePrice(String lowerTitle) {
        if (lowerTitle.contains("xe")) {
            return BigDecimal.valueOf(300_000_000L);
        } else if (lowerTitle.contains("pin") || lowerTitle.contains("battery")) {
            return BigDecimal.valueOf(50_000_000L);
        } else if (lowerTitle.contains("động cơ") || lowerTitle.contains("motor")) {
            return BigDecimal.valueOf(20_000_000L);
        } else if (lowerTitle.contains("bộ sạc") || lowerTitle.contains("charger")) {
            return BigDecimal.valueOf(10_000_000L);
        } else if (lowerTitle.contains("bình")) {
            return BigDecimal.valueOf(15_000_000L);
        } else if (lowerTitle.contains("linh kiện") || lowerTitle.contains("phụ tùng")) {
            return BigDecimal.valueOf(8_000_000L);
        }

        return BigDecimal.valueOf(5_000_000L); // Mặc định
    }

    /**
     * Xác định factor giá theo tình trạng
     */
    private BigDecimal determinePriceFactor(String lowerTitle) {
        if (lowerTitle.contains("mới") || lowerTitle.contains("100%")) {
            return BigDecimal.valueOf(0.95);
        } else if (lowerTitle.contains("90%") || lowerTitle.contains("như mới")) {
            return BigDecimal.valueOf(0.85);
        } else if (lowerTitle.contains("80%")) {
            return BigDecimal.valueOf(0.75);
        } else if (lowerTitle.contains("70%")) {
            return BigDecimal.valueOf(0.65);
        } else if (lowerTitle.contains("60%") || lowerTitle.contains("trung bình")) {
            return BigDecimal.valueOf(0.55);
        } else if (lowerTitle.contains("cũ") || lowerTitle.contains("đã qua sử dụng")) {
            return BigDecimal.valueOf(0.70);
        }

        return BigDecimal.ONE; // Mặc định 100%
    }
}