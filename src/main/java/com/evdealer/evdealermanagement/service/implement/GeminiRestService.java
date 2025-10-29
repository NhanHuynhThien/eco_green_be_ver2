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
        this.maxTokens = Integer.parseInt(dotenv.get("GEMINI_MAX_TOKENS", "8192"));
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

    // ========== Suggest Price, Title & Description ==========

    public PriceSuggestion suggestPrice(String title, String vehicleModel, String versionName,
                                        String batteryHealth, String mileageKm,
                                        String brand, String manufactureYear) {

        if (vehicleModel == null || vehicleModel.trim().isEmpty()) {
            log.warn("⚠️ `vehicleModel` is empty in the request. This will reduce suggestion accuracy.");
        }

        String prompt = buildPricePrompt(title, vehicleModel, versionName, batteryHealth, mileageKm, brand, manufactureYear);

        try {
            log.info("=== GEMINI REST API REQUEST (Price, Title, Desc) ===");
            log.info("Prompting for title: {}", title);

            // <<< FIX #1: Corrected typo in URL >>>
            String url = String.format(
                    "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                    this.modelName, apiKey);

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
                    url, HttpMethod.POST, request, String.class);

            log.info("✅ Response status: {}", response.getStatusCode());

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
     * <<< PROMPT REFINED to enforce higher depreciation >>>
     * Builds a highly-controlled prompt focused on "on-road costs" to ensure realistic used car pricing.
     */
    private String buildPricePrompt(String title, String vehicleModel, String versionName,
                                    String batteryHealth, String mileageKm,
                                    String brand, String manufactureYear) {
        return String.format(
                """
                Bạn là chuyên gia thẩm định giá xe điện CŨ tại Việt Nam với 15+ năm kinh nghiệm, cực kỳ am hiểu về thị trường và tâm lý người mua.
                
                **QUY TẮC BẤT DI BẤT DỊCH (TUÂN THỦ TUYỆT ĐỐI):**
                1.  **QUY TẮC "CHI PHÍ LĂN BÁNH"**: Ngay khi một chiếc xe mới được đăng ký ra biển số, nó đã mất ngay lập tức 10-15%% giá trị do các chi phí không thể thu hồi (thuế trước bạ, phí biển số, đăng kiểm...). Đây là mức khấu hao TỐI THIỂU cho bất kỳ chiếc xe nào đã qua sử dụng, dù chỉ mới đi 1km.
                2.  **QUY TẮC "KHẤU HAO THỊ TRƯỜNG"**: Dựa trên Quy tắc 1, cộng thêm khấu hao do năm sử dụng, ODO, và tình trạng pin, giá bán cuối cùng của xe cũ PHẢI PHẢN ÁNH MỨC KHẤU HAO TỔNG CỘNG **ÍT NHẤT TỪ 15-25%%** so với giá xe mới. Người mua xe cũ tìm kiếm một món hời, không phải một chiếc xe mới rẻ hơn một chút.
                
                **NHIỆM VỤ:**
                Dựa trên thông tin xe và 2 QUY TẮC VÀNG trên, hãy cung cấp các mục sau:
                1.  **Tiêu đề bán hàng**: Hấp dẫn, SEO-friendly, tối đa 50 ký tự.
                2.  **Giá xe mới tham khảo**: Nêu rõ giá xe mới nhất của phiên bản này để làm cơ sở.
                3.  **Khoảng giá bán đề xuất**: BẮT BUỘC định dạng `[Giá thấp nhất] - [Giá cao nhất] VNĐ` và PHẢI THẤP HƠN GIÁ MỚI ít nhất 15-25%%.
                4.  **Mô tả sản phẩm**: **Tối đa 5-7 câu**, tập trung vào các điểm nổi bật nhất.
                5.  **Giải thích lý do định giá**: CỰC NGẮN GỌN, đề cập đến việc đã áp dụng "chi phí lăn bánh" và khấu hao thị trường.
                6.  **Link tham khảo**: CUNG CẤP LINK TÌM KIẾM LÀ BẮT BUỘC.
                
                **THÔNG TIN XE:**
                - Tiêu đề gốc: %s
                - Hãng: %s
                - Model: %s
                - Phiên bản: %s
                - Năm sản xuất: %s
                - Tình trạng pin: %s
                - Số km đã đi: %s
                
                **ĐỊNH DẠNG LINK THAM KHẢO (BẮT BUỘC):**
                1. Chợ Tốt: https://www.chotot.com/mua-ban-oto?q=[Hãng]+[Model]
                2. Bonbanh: https://bonbanh.com/tim-kiem?q=[Hãng]+[Model]
                3. Oto.com.vn: https://oto.com.vn/mua-ban-xe-[hang]-[model]
                
                **===== ĐỊNH DẠNG OUTPUT (TUÂN THỦ NGHIÊM NGẶT) =====**
                Tiêu đề gợi ý: [Nội dung]
                Giá xe mới tham khảo: [Giá xe mới] VNĐ
                Giá gợi ý: [Khoảng giá] VNĐ
                Mô tả sản phẩm: [Nội dung mô tả ngắn gọn]
                Giải thích lý do: [Nội dung giải thích]
                Nguồn tham khảo:
                [Link 1]
                [Link 2]
                [Link 3]
                """,
                title, brand, vehicleModel, versionName, manufactureYear, batteryHealth, mileageKm
        );
    }

    private PriceSuggestion handleSuccessResponse(String responseBody, String title) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            if (root.has("error")) {
                log.error("API Error: {} - {}", root.path("error").path("code").asInt(), root.path("error").path("message").asText());
                return generateFallback(title);
            }

            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode firstCandidate = candidates.get(0);
                String finishReason = firstCandidate.path("finishReason").asText("UNKNOWN");
                log.info("Finish Reason: {}", finishReason);

                if ("STOP".equals(finishReason) || "MAX_TOKENS".equals(finishReason)) {
                    String text = firstCandidate.at("/content/parts/0/text").asText();
                    if (!text.isEmpty()) {
                        log.info("Gemini response text received (Finish Reason: {})", finishReason);
                        if ("MAX_TOKENS".equals(finishReason)) {
                            log.warn("⚠️ Response was cut short due to MAX_TOKENS limit. Results might be incomplete.");
                        }
                        return parseResponse(text, title);
                    }
                } else {
                    log.warn("⚠️ Response may be incomplete or blocked. Reason: {}", finishReason);
                    return generateFallback(title);
                }
            }
            log.error("Unexpected response format: no valid content found");
            return generateFallback(title);
        } catch (Exception e) {
            log.error("Error parsing Gemini response: {}", e.getMessage(), e);
            return generateFallback(title);
        }
    }

    private PriceSuggestion parseResponse(String rawText, String originalTitle) {
        String nextHeadings = "\\s*Giá xe mới tham khảo:|\\s*Giá gợi ý:|\\s*Mô tả sản phẩm:|\\s*Giải thích lý do:|\\s*Nguồn tham khảo:|\\s*$";
        Pattern titlePattern = Pattern.compile("Tiêu đề gợi ý:\\s*(.+?)(?=" + nextHeadings + ")", Pattern.DOTALL);
        Pattern pricePattern = Pattern.compile("Giá gợi ý:\\s*(.+?)(?=" + nextHeadings + ")", Pattern.DOTALL);
        Pattern descriptionPattern = Pattern.compile("Mô tả sản phẩm:\\s*(.+?)(?=" + nextHeadings + ")", Pattern.DOTALL);
        Pattern reasonPattern = Pattern.compile("Giải thích lý do:\\s*(.+?)(?=" + nextHeadings + ")", Pattern.DOTALL);
        Pattern sourcesPattern = Pattern.compile("Nguồn tham khảo:\\s*([\\s\\S]+)", Pattern.DOTALL);

        Matcher titleMatcher = titlePattern.matcher(rawText);
        Matcher priceMatcher = pricePattern.matcher(rawText);
        Matcher descriptionMatcher = descriptionPattern.matcher(rawText);
        Matcher reasonMatcher = reasonPattern.matcher(rawText);
        Matcher sourcesMatcher = sourcesPattern.matcher(rawText);

        String title = titleMatcher.find() ? cleanText(titleMatcher.group(1)) : originalTitle;
        String priceStr = priceMatcher.find() ? cleanPrice(priceMatcher.group(1)) : "Không xác định được giá";
        String description = descriptionMatcher.find() ? cleanText(descriptionMatcher.group(1)) : "Chưa có mô tả chi tiết.";
        String reason = reasonMatcher.find() ? cleanText(reasonMatcher.group(1)) : "Không có giải thích chi tiết.";

        List<String> sources = new ArrayList<>();
        if (sourcesMatcher.find()) {
            String sourcesText = sourcesMatcher.group(1).trim();
            sources = Arrays.stream(sourcesText.split("\\n"))
                    .map(String::trim)
                    .filter(s -> s.startsWith("http"))
                    .filter(this::isValidCarSalesSource)
                    .collect(Collectors.toList());
        } else {
            log.warn("Could not find 'Nguồn tham khảo:' block in the response.");
        }

        log.info("✅ Parsed → Title: [{}], Price: [{}], Sources: {}", title, priceStr, sources.size());
        return new PriceSuggestion(priceStr, reason, sources, description, title);
    }

    private String cleanPrice(String priceText) {
        String cleaned = priceText.replaceAll("[*`]", "").replaceAll("\\s{2,}", " ").trim();
        if (!cleaned.toUpperCase().endsWith("VNĐ") && !cleaned.toUpperCase().endsWith("VND")) {
            cleaned += " VNĐ";
        }
        return cleaned;
    }

    private boolean isValidCarSalesSource(String url) {
        if (url == null || url.isEmpty()) return false;
        String lowerUrl = url.toLowerCase();
        List<String> validDomains = Arrays.asList("chotot.com", "bonbanh.com", "carmudi.vn", "oto.com.vn", "choxe.vn", "muaban.net");
        List<String> invalidKeywords = Arrays.asList("facebook", "google", "support", "youtube", "zalo");
        boolean isValid = validDomains.stream().anyMatch(lowerUrl::contains) && invalidKeywords.stream().noneMatch(lowerUrl::contains);
        if (!isValid) {
            log.debug("❌ Filtered invalid source: {}", url);
        }
        return isValid;
    }

    private String cleanText(String text) {
        return text.replaceAll("[*`\n\r]", " ").replaceAll("\\s{2,}", " ").trim();
    }

    private PriceSuggestion generateFallback(String title) {
        log.warn("⚠️ Using fallback response for: {}", title);
        String priceStr = "Không thể gợi ý giá";
        String reason = "Đã có lỗi xảy ra trong quá trình kết nối đến dịch vụ gợi ý. Vui lòng thử lại sau.";
        String description = "Thông tin chi tiết đang được cập nhật.";
        List<String> fallbackSources = Arrays.asList("https://www.chotot.com/mua-ban-oto", "https://bonbanh.com", "https://oto.com.vn");
        return new PriceSuggestion(priceStr, reason, fallbackSources, description, title);
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