package com.evdealer.evdealermanagement.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MomoResponse {
    private String payUrl; // URL redirect người dùng tới MoMo
    private String deeplink; // Link mở app MoMo (nếu có)
    private String qrCodeUrl; // Link QR code (nếu có)
    private Integer resultCode; // 0 = thành công
    private String message; // mô tả
    private String orderId;
    private String requestId;
}
