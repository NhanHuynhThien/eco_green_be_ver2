package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.price.PriceSuggestion;
import com.evdealer.evdealermanagement.dto.vehicle.catalog.VehicleCatalogDTO;
import com.evdealer.evdealermanagement.entity.vehicle.Model;
import com.evdealer.evdealermanagement.entity.vehicle.VehicleCatalog;
import com.evdealer.evdealermanagement.repository.VehicleModelRepository;
import com.evdealer.evdealermanagement.utils.PriceSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.lang.Maps;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class GeminiRestService {

    private final Dotenv dotenv;

    private String apiKey;
    private String modelName;
    private int maxTokens;
    private float temperature;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final VehicleModelRepository vehicleModelRepository;

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

    // ========== Sugget Price ==========

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
                    modelName, apiKey);

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", prompt)))),
                    "generationConfig", Map.of(
                            "temperature", temperature,
                            "maxOutputTokens", maxTokens,
                            "topP", 0.9,
                            "topK", 40));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class);

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
                        + "Sản phẩm: %s",
                title);
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
                        error.path("message").asText());
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
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

        // Pattern để match "Mô tả ngắn gọn: ..."
        Pattern reasonPattern = Pattern.compile(
                "Mô tả ngắn gọn:\\s*(.+?)(?=\\n|$)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

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
                .replaceAll("\\*+", "") // Bỏ dấu *
                .replaceAll("\\n+", " ") // Thay newline bằng space
                .replaceAll("\\s{2,}", " ") // Bỏ multiple spaces
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
                factor.multiply(BigDecimal.valueOf(100)).doubleValue());

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

    // ========== Suggest Price ==========
    public String buildSpecsPrompt(String productName, String modelName, String brand, String version, Short year) {
        return String.format(
                """
                        Bạn là chuyên gia xe điện.
                        Hãy dựa vào tên sản phẩm "%s", model "%s", thương hiệu "%s", phiên bản "%s", và năm sản xuất "%d" để trả về thông số kỹ thuật chuẩn dưới dạng JSON.
                        KHÔNG thêm lời giải thích, markdown, hoặc bất kỳ ký tự nào ngoài JSON thuần túy.

                        Cấu trúc JSON cần có CHÍNH XÁC các trường sau:
                        {
                          "model": "Tên đầy đủ của model",
                          "type": "Loại xe (VD: SUV/Crossover, Scooter, Sedan, Hatchback, Xe máy điện)",
                          "color": "Màu sắc phổ biến",
                          "range_km": "Tầm hoạt động thực tế (số km, không có đơn vị)",
                          "battery_capacity_kwh": "Dung lượng pin (số kWh, không có đơn vị)",
                          "charging_time_hours": "Thời gian sạc đầy pin (giờ, không có đơn vị)",
                          "motor_power_w": "Công suất động cơ (W, không có đơn vị)",
                          "built_in_battery_capacity_ah": "Dung lượng pin tích hợp theo xe (số Ah, không có đơn vị)",
                          "built_in_battery_voltage_v": "Điện áp danh định của pin (V, không có đơn vị)",
                          "removable_battery": "Pin có thể tháo rời hay không (true/false)",
                          "power_hp": "Công suất tối đa (số hp, không có đơn vị)",
                          "top_speed_kmh": "Tốc độ tối đa (số km/h, không có đơn vị)",
                          "acceleration_0_100_s": "Thời gian tăng tốc 0-100km/h (số giây, không có đơn vị, để null nếu là xe máy)",
                          "weight_kg": "Trọng lượng bản thân (số kg, không có đơn vị)",
                          "gross_weight_kg": "Trọng lượng toàn tải (số kg, không có đơn vị)",
                          "length_mm": "Chiều dài tổng thể (số mm, không có đơn vị)",
                          "wheelbase_mm": "Chiều dài cơ sở (số mm, không có đơn vị)",
                          "features": ["Tính năng 1", "Tính năng 2", "Tính năng 3", "Tính năng 4", "Tính năng 5"]
                        }

                        QUY TẮC BẮT BUỘC:
                        - TẤT CẢ các trường số phải là số nguyên hoặc số thực, KHÔNG có đơn vị, KHÔNG có dấu phẩy phân cách hàng nghìn
                        - Trường "features" phải là mảng string, mỗi tính năng là 1 câu ngắn gọn, từ 5-10 tính năng
                        - Nếu không có thông tin chính xác, hãy ước lượng dựa trên xe cùng phân khúc và năm sản xuất
                        - Nếu là xe máy điện: để null cho "acceleration_0_100_s", điều chỉnh các thông số phù hợp
                        - CHỈ trả về JSON thuần túy, KHÔNG có ```json, KHÔNG có giải thích, KHÔNG có markdown

                        Ví dụ output mong muốn:
                        {
                          "model": "VF e34",
                          "type": "SUV/Crossover",
                          "color": "Xanh",
                          "range_km": 285,
                          "battery_capacity_kwh": 42,
                          "power_hp": 110,
                          "top_speed_kmh": 145,
                          "acceleration_0_100_s": 9.5,
                          "weight_kg": 1450,
                          "gross_weight_kg": 1890,
                          "length_mm": 4300,
                          "wheelbase_mm": 2611,
                          "features": ["Hệ thống phanh ABS", "Hỗ trợ đỗ xe tự động", "Màn hình cảm ứng 10 inch", "Kết nối smartphone", "Camera 360 độ", "Cảnh báo điểm mù", "Túi khí an toàn"]
                        }
                        """,
                productName, modelName, brand, version, year);
    }

    public String suggestSpecs(String productName, String modelName, String brand, String version, Short year) {
        String prompt = buildSpecsPrompt(productName, modelName, brand, version, year);

        try {
            log.info("=== GEMINI REQUEST: Suggest Vehicle Specs ===");
            log.info("Product: {}, Model: {}, Brand: {}, Version: {}, Year: {}", productName, modelName, brand, version,
                    year);

            String url = String.format(
                    "https://generativelanguage.googleapis.com/v1/models/%s:generateContent?key=%s",
                    this.modelName, apiKey);

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", prompt)))),
                    "generationConfig", Map.of(
                            "temperature", temperature,
                            "maxOutputTokens", maxTokens,
                            "topK", 40,
                            "topP", 0.9));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode textNode = root.at("/candidates/0/content/parts/0/text");

                if (!textNode.isMissingNode()) {
                    String result = textNode.asText().trim();
                    log.info("Raw Gemini response: {}", result);
                    return result;
                }
            }

            log.warn("No valid response from Gemini API");
        } catch (JsonProcessingException e) {
            log.error("Error while generating specs: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error calling Gemini API: {}", e.getMessage(), e);
        }

        return "{}";
    }

    public VehicleCatalogDTO getVehicleSpecs(String productName, String modelName, String brand, String version,
            Short year) {
        try {
            String json = suggestSpecs(productName, modelName, brand, version, year);
            // Làm sạch dữ liệu Gemini trả về
            if (json.startsWith("```")) {
                json = json.replaceAll("```json", "")
                        .replaceAll("```", "")
                        .trim();
            }
            Model model = vehicleModelRepository.findByName(productName);
            VehicleCatalogDTO dto = objectMapper.readValue(json, VehicleCatalogDTO.class);
            dto.setModel(model);

            log.info("Cleaned JSON before parsing:\n{}", json);

            return dto;

        } catch (JsonProcessingException e) {
            log.error("Failed to parse specs JSON for '{}': {}", productName, e.getMessage());
            Model model = vehicleModelRepository.findByName(productName);
            return VehicleCatalogDTO.builder()
                    .model(model)
                    .type("Cannot define")
                    .features(List.of("Chưa có dữ liệu"))
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error while generating specs: {}", e.getMessage(), e);
            Model model = vehicleModelRepository.findByName(productName);
            return VehicleCatalogDTO.builder()
                    .model(model)
                    .type("Cannot define")
                    .features(List.of("Chưa có dữ liệu"))
                    .build();
        }
    }
}