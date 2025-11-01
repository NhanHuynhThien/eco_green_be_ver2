package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.price.PriceSuggestion;
import com.evdealer.evdealermanagement.dto.vehicle.catalog.VehicleCatalogDTO;
import com.evdealer.evdealermanagement.entity.vehicle.Model;
import com.evdealer.evdealermanagement.repository.VehicleModelRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        this.modelName = dotenv.get("GEMINI_MODEL", "gemini-1.5-flash-latest");
        this.maxTokens = Integer.parseInt(dotenv.get("GEMINI_MAX_TOKENS", "8096"));
        this.temperature = Float.parseFloat(dotenv.get("GEMINI_TEMPERATURE", "0.7"));

        log.info("=== GEMINI REST SERVICE INITIALIZED ===");
        log.info("Model: {}", modelName);
        log.info("MaxTokens: {}", maxTokens);
        log.info("Temperature: {}", temperature);

        if (apiKey == null || apiKey.isEmpty()) {
            log.error("❌ GEMINI_API_KEY not found in .env file!");
            throw new IllegalStateException("GEMINI_API_KEY is required");
        }
    }

    // ========== Suggest Price, Title & Description ==========

    public PriceSuggestion suggestPrice(
            String vehicleModel,
            String versionName,
            String batteryHealth,
            String mileageKm,
            String brandName,
            String manufactureYear) {

        if (vehicleModel == null || vehicleModel.trim().isEmpty()) {
            log.warn("⚠️ vehicleModel is empty - using fallback");
            return generateFallback("Unknown Vehicle");
        }

        String prompt = buildPricePrompt(vehicleModel, versionName, batteryHealth,
                mileageKm, brandName, manufactureYear);

        try {
            log.info("=== GEMINI API REQUEST: Price Suggestion ===");
            log.info("Vehicle: {} {} {} ({})", brandName, vehicleModel, versionName, manufactureYear);

            String url = String.format(
                    "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                    this.modelName, apiKey);

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", prompt)))
                    ),
                    "generationConfig", Map.of(
                            "temperature", temperature,
                            "maxOutputTokens", maxTokens,
                            "topP", 0.95,
                            "topK", 40
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, String.class);

            log.info("✅ Response status: {}", response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return handleSuccessResponse(response.getBody(), vehicleModel);
            } else {
                log.error("❌ Unexpected response status: {}", response.getStatusCode());
                return generateFallback(vehicleModel);
            }

        } catch (Exception e) {
            log.error("❌ Error calling Gemini API: {}", e.getMessage(), e);
            return generateFallback(vehicleModel);
        }
    }

    /**
     * Build optimized prompt for price suggestion with JSON output
     */
    private String buildPricePrompt(String vehicleModel, String versionName,
                                    String batteryHealth, String mileageKm,
                                    String brandName, String manufactureYear) {
        if (versionName == null) {
            versionName = "Phiên bản tiêu chuẩn, mặc định của nhà sản xuất";
        }

        return String.format("""
                Bạn là chuyên gia định giá xe điện cũ tại Việt Nam.
                
                XE CẦN ĐỊNH GIÁ:
                - Hãng: %s | Model: %s | Version: %s
                - Năm: %s | Pin: %s | Km đã đi: %s
                
                QUY TẮC KHẤU HAO:
                1. Ra biển số: -10-15%%
                2. Mỗi năm: -8-12%%
                3. Km > 20k/năm: -3-5%%
                4. Pin < 90%%: -5-10%%
                → Tổng: -20-35%% so với giá mới
                
                YÊU CẦU: Trả về JSON THUẦN TÚY (không thêm ```json):
                {
                  "title": "%s %s %s %s",
                  "newPrice": "Giá mới VNĐ",
                  "suggestedPrice": "X - Y VNĐ",
                  "description": "Mô tả ngắn gọn 80-120 từ",
                  "reason": "Công thức: Giá mới - khấu hao = giá cũ",
                  "sources": [
                    "https://www.chotot.com/mua-ban-oto?q=%s+%s",
                    "https://bonbanh.com/oto-cu/%s-%s",
                    "https://oto.com.vn/mua-ban-xe"
                  ]
                }
                """,
                brandName, vehicleModel, versionName, manufactureYear, batteryHealth, mileageKm,
                brandName, vehicleModel, versionName, manufactureYear,
                brandName, vehicleModel,
                brandName.toLowerCase().replace(" ", "-"),
                vehicleModel.toLowerCase().replace(" ", "-"));
    }

    /**
     * Handle successful API response with proper finishReason check
     */
    private PriceSuggestion handleSuccessResponse(String responseBody, String vehicleModel) {
        try {
            log.debug("Raw response body: {}", responseBody);

            JsonNode root = objectMapper.readTree(responseBody);

            // Check for API errors
            if (root.has("error")) {
                JsonNode error = root.path("error");
                String errorMsg = error.path("message").asText();
                log.error("❌ Gemini API Error: {}", errorMsg);
                return generateFallback(vehicleModel);
            }

            // Get candidates array
            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                log.error("❌ No candidates in response");
                return generateFallback(vehicleModel);
            }

            JsonNode firstCandidate = candidates.get(0);
            String finishReason = firstCandidate.path("finishReason").asText("UNKNOWN");

            log.info("📊 Finish Reason: {}", finishReason);

            // ✅ CRITICAL: Only process if completed successfully
            if (!"STOP".equals(finishReason)) {
                log.error("❌ Response incomplete. Reason: {}", finishReason);

                switch (finishReason) {
                    case "MAX_TOKENS":
                        log.error("💡 Solution: Reduce GEMINI_MAX_TOKENS to 1500 or optimize prompt");
                        break;
                    case "SAFETY":
                        log.error("⚠️ Content blocked by safety filters");
                        break;
                    case "RECITATION":
                        log.error("⚠️ Content flagged for copyright");
                        break;
                }

                return generateFallback(vehicleModel);
            }

            // Extract text content
            JsonNode textNode = firstCandidate.at("/content/parts/0/text");
            if (textNode.isMissingNode() || textNode.asText().trim().isEmpty()) {
                log.error("❌ Empty text content");
                return generateFallback(vehicleModel);
            }

            String text = textNode.asText().trim();
            log.info("✅ Received complete response ({} chars)", text.length());

            return parseJsonResponse(text, vehicleModel);

        } catch (JsonProcessingException e) {
            log.error("❌ JSON parsing error: {}", e.getMessage());
            return generateFallback(vehicleModel);
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            return generateFallback(vehicleModel);
        }
    }

    /**
     * Parse JSON response from Gemini
     */
    private PriceSuggestion parseJsonResponse(String rawText, String vehicleModel) {
        try {
            // Clean markdown wrapper if present
            String cleanedJson = rawText
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*$", "")
                    .trim();

            // Remove any leading/trailing whitespace or newlines
            if (cleanedJson.startsWith("{") && cleanedJson.endsWith("}")) {
                log.debug("Valid JSON detected, parsing...");
            } else {
                log.warn("Response doesn't look like valid JSON, attempting to extract...");
                // Try to find JSON block within text
                int start = cleanedJson.indexOf("{");
                int end = cleanedJson.lastIndexOf("}");
                if (start >= 0 && end > start) {
                    cleanedJson = cleanedJson.substring(start, end + 1);
                }
            }

            log.debug("Cleaned JSON: {}", cleanedJson);

            JsonNode root = objectMapper.readTree(cleanedJson);

            // Extract fields with fallbacks
            String title = root.path("title").asText(vehicleModel + " - Xe Điện Cũ");
            String newPrice = root.path("newPrice").asText("Chưa xác định");
            String suggestedPrice = root.path("suggestedPrice").asText("Liên hệ để biết giá");
            String description = root.path("description").asText("Xe điện chất lượng, giá hợp lý.");
            String reason = root.path("reason").asText("Giá được tính dựa trên thị trường hiện tại.");

            // Parse sources array
            List<String> sources = parseSources(root.path("sources"));

            // Fallback sources
            if (sources.isEmpty()) {
                sources = getDefaultSources();
            }

            log.info("✅ Successfully parsed: {}", title);

            return new PriceSuggestion(suggestedPrice, reason, sources, description, title);

        } catch (JsonProcessingException e) {
            log.error("❌ Failed to parse JSON: {}", e.getMessage());
            log.error("Raw text was: {}", rawText);
            return generateFallback(vehicleModel);
        }
    }

    /**
     * Parse sources array from JSON node
     */
    private List<String> parseSources(JsonNode sourcesNode) {
        List<String> sources = new ArrayList<>();
        if (sourcesNode.isArray()) {
            for (JsonNode node : sourcesNode) {
                String url = node.asText();
                if (isValidCarSalesSource(url)) {
                    sources.add(url);
                }
            }
        }
        return sources;
    }

    /**
     * Validate car sales source URLs
     */
    private boolean isValidCarSalesSource(String url) {
        if (url == null || url.isEmpty()) return false;
        String lower = url.toLowerCase();
        List<String> validDomains = List.of(
                "chotot.com", "bonbanh.com", "oto.com.vn",
                "carmudi.vn", "choxe.vn"
        );
        return validDomains.stream().anyMatch(lower::contains);
    }

    /**
     * Get default reference sources
     */
    private List<String> getDefaultSources() {
        return List.of(
                "https://www.chotot.com/mua-ban-oto",
                "https://bonbanh.com/oto-cu",
                "https://oto.com.vn/mua-ban-xe"
        );
    }

    /**
     * Generate fallback response when API fails
     */
    private PriceSuggestion generateFallback(String vehicleModel) {
        String title = vehicleModel + " - Xe Điện Cũ";
        String reason = "Không thể kết nối đến Gemini API hoặc dữ liệu chưa đầy đủ. " +
                "Vui lòng thử lại sau hoặc liên hệ để được tư vấn trực tiếp.";
        String description = "Xe điện " + vehicleModel + " đang được cập nhật thông tin định giá. " +
                "Vui lòng liên hệ để được báo giá chính xác nhất.";

        log.warn("⚠️ Returning fallback response for: {}", vehicleModel);

        return new PriceSuggestion(
                "Liên hệ để biết giá",
                reason,
                getDefaultSources(),
                description,
                title
        );
    }

    // ========== Suggest Specs ==========

    /**
     * Xây dựng prompt để gợi ý thông số kỹ thuật
     */
    public String buildSpecsPrompt(String productName, String vehicleModel, String brand, String version, Short year) {
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
                productName, vehicleModel, brand, version, year);
    }

    /**
     * Gọi Gemini API để gợi ý thông số kỹ thuật
     */
    public String suggestSpecs(String productName, String vehicleModel, String brand, String version, Short year) {
        String prompt = buildSpecsPrompt(productName, vehicleModel, brand, version, year);

        try {
            log.info("=== GEMINI REQUEST: Suggest Vehicle Specs ===");
            log.info("Product: {}, Model: {}, Brand: {}, Version: {}, Year: {}",
                    productName, vehicleModel, brand, version, year);

            String url = String.format(
                    "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                    this.modelName, apiKey); // Use this.modelName

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
                    log.info("Raw Gemini specs response received");
                    return result;
                }
            }

            log.warn("No valid response from Gemini API for specs");
        } catch (JsonProcessingException e) {
            log.error("Error while generating specs: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error calling Gemini API: {}", e.getMessage(), e);
        }

        return "{}";
    }

    /**
     * Lấy thông số kỹ thuật xe và map thành VehicleCatalogDTO
     */
    public VehicleCatalogDTO getVehicleSpecs(String productName, String vehicleModel, String brand, String version,
                                             Short year) {
        try {
            String json = suggestSpecs(productName, vehicleModel, brand, version, year);

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