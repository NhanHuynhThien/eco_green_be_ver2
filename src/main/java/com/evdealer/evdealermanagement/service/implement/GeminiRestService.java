package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.price.PriceSuggestion;
import com.evdealer.evdealermanagement.entity.vehicle.VehicleSpecs;
import com.evdealer.evdealermanagement.utils.PriceSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.lang.Maps;
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
        this.modelName = dotenv.get("GEMINI_MODEL", "gemini-2.5-flash");
        this.maxTokens = Integer.parseInt(dotenv.get("GEMINI_MAX_TOKENS", "4096")); // ✅ Tăng từ 1000 lên 4096
        this.temperature = Float.parseFloat(dotenv.get("GEMINI_TEMPERATURE", "0.5"));

        log.info("=== GEMINI REST SERVICE INITIALIZED ===");
        log.info("Model: {}", modelName);
        log.info("MaxTokens: {}", maxTokens);
        log.info("Temperature: {}", temperature);

        if (apiKey == null || apiKey.isEmpty()) {
            log.error("❌ GEMINI_API_KEY not found in .env file!");
            throw new IllegalStateException("GEMINI_API_KEY is required");
        }
    }


    //========== Sugget Price ==========

    /**
     * Gợi ý giá cho sản phẩm dựa trên tiêu đề
     *
     * @param title Tiêu đề sản phẩm
     * @return PriceSuggestion chứa giá và mô tả
     */
    public PriceSuggestion suggestPrice(String title) {
        String prompt = buildPricePrompt(title);

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
    private String buildPricePrompt(String title) {
        return String.format(
                "Bạn là chuyên gia thẩm định giá sản phẩm cũ tại Việt Nam. "
                        + "Hãy dựa trên tiêu đề sản phẩm để đưa ra: "
                        + "1. Giá gợi ý hợp lý (đơn vị VNĐ) "
                        + "2. Mô tả ngắn gọn tình trạng (1 câu). "
                        + "Chỉ trả lời đúng format sau:\n"
                        + "Giá gợi ý: [giá hoặc khoảng giá] VNĐ\n"
                        + "Mô tả ngắn gọn trong 1-2 câu: [mô tả]\n\n"
                        + "Sản phẩm: %s", title
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

            // Lấy candidates từ response
            JsonNode candidates = root.path("candidates");

            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);

                // Kiểm tra finishReason để biết tại sao bị block
                JsonNode finishReason = firstCandidate.path("finishReason");
                if (!finishReason.isMissingNode()) {
                    String reason = finishReason.asText();
                    log.info("Finish Reason: {}", reason);

                    // Nếu bị block bởi safety
                    if (!"STOP".equals(reason)) {
                        log.warn("⚠️ Response blocked/filtered. Reason: {}", reason);

                        // Kiểm tra safety ratings nếu có
                        JsonNode safetyRatings = firstCandidate.path("safetyRatings");
                        if (safetyRatings.isArray()) {
                            log.info("Safety ratings: {}", safetyRatings);
                        }

                        return generateFallback(title);
                    }
                }

                // Lấy content
                JsonNode content = firstCandidate.path("content");

                if (!content.isMissingNode()) {
                    JsonNode parts = content.path("parts");

                    // Kiểm tra parts có phần tử không
                    if (parts.isArray() && parts.size() > 0) {
                        JsonNode textNode = parts.get(0).path("text");

                        if (!textNode.isMissingNode() && !textNode.asText().isEmpty()) {
                            String text = textNode.asText();
                            log.info("✅ Gemini response text received");
                            return parseResponse(text, title);
                        }
                    } else {
                        log.error("❌ Parts is empty or not an array");
                    }
                } else {
                    log.error("❌ Content node is missing");
                }
            } else {
                log.error("❌ Candidates array is empty or missing");
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

        return BigDecimal.ONE;
    }


    //========== Suggest Price ==========
    public String buildSpecsPrompt(String productName) {
        return String.format("""
                Bạn là chuyên gia xe điện. 
                Hãy dựa vào tên sản phẩm "%s" để trả về thông số kỹ thuật chuẩn dưới dạng JSON, 
                KHÔNG thêm lời giải thích nào khác.
                Các trường cần có:
                {
                  "model": "Tên sản phẩm",
                  "type": "Loại xe (VD: SUV/Crossover, Scooter, Sedan, Hatchback...)",
                  "color": "Màu phổ biến",
                  "range_km": "Tầm hoạt động (km)",
                  "battery_capacity_kwh": "Dung lượng pin (kWh)",
                  "power_hp": "Công suất (hp)",
                  "top_speed_kmh": "Tốc độ tối đa (km/h)",
                  "acceleration_0_100_s": "Thời gian tăng tốc 0-100 (giây)",
                  "weight_kg": "Trọng lượng bản thân (kg)",
                  "gross_weight_kg": "Trọng lượng toàn tải (kg)",
                  "length_mm": "Chiều dài (mm)",
                  "wheelbase_mm": "Chiều dài cơ sở (mm)",
                  "features": ["Danh sách 5-10 tính năng phổ biến"]
                }
                
                ⚠️ Lưu ý: 
                - Nếu là xe máy điện hoặc pin điện, hãy chỉ trả thông số phù hợp.
                - Chỉ trả đúng JSON, không thêm chữ nào khác.
                """, productName);
    }

    public String suggestSpecs(String productName) {
        String prompt = buildSpecsPrompt(productName);

        try {
            log.info("=== GEMINI REQUEST: Suggest Vehicle Specs ===");
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
                            "topK", 40,
                            "topP", 0.9
                    )
            );


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode textNode = root.at("/candidates/0/content/parts/0/text");

                if (!textNode.isMissingNode()) {
                    return textNode.asText().trim();
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Error while generating specs: {}", e.getMessage(), e);
        }
        return "{}";
    }

    public VehicleSpecs getVehicleSpecs(String productName) {
        try {
            String json = suggestSpecs(productName);

            // 🧹 Làm sạch dữ liệu Gemini trả về
            if (json.startsWith("```")) {
                json = json.replaceAll("```json", "")
                        .replaceAll("```", "")
                        .trim();
            }

            log.info("✅ Cleaned JSON before parsing:\n{}", json);

            return objectMapper.readValue(json, VehicleSpecs.class);

        } catch (JsonProcessingException e) {
            log.error("❌ Failed to parse specs JSON for '{}': {}", productName, e.getMessage());
            return VehicleSpecs.builder()
                    .model(productName)
                    .type("Không xác định")
                    .features(List.of("Chưa có dữ liệu"))
                    .build();
        } catch (Exception e) {
            log.error("❌ Unexpected error while generating specs: {}", e.getMessage(), e);
            return VehicleSpecs.builder()
                    .model(productName)
                    .type("Không xác định")
                    .features(List.of("Chưa có dữ liệu"))
                    .build();
        }
    }

}