package com.evdealer.evdealermanagement.dto.price;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO chứa thông tin gợi ý giá, tiêu đề, và mô tả từ AI.
 * Đã bao gồm các trường title và description.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceSuggestion {
    private String price;
    private String reason;
    private List<String> sources;

    private String description; // Mô tả sản phẩm chi tiết
    private String title;       // Tiêu đề gợi ý

    /**
     * Constructor cũ để tương thích ngược.
     * @param price Giá
     * @param reason Lý do
     */
    public PriceSuggestion(String price, String reason) {
        this.price = price;
        this.reason = reason;
        this.sources = List.of();
        this.description = "Chưa có mô tả chi tiết."; // Giá trị mặc định
        this.title = "Tiêu đề chưa được gợi ý"; // Giá trị mặc định
    }
}
