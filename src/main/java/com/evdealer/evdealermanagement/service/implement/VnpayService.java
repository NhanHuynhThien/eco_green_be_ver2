package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.configurations.VnpayConfig;
import com.evdealer.evdealermanagement.configurations.vnpay.VnpayProperties;
import com.evdealer.evdealermanagement.dto.payment.VnpayRequest;
import com.evdealer.evdealermanagement.dto.payment.VnpayResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VnpayService {

    private final VnpayProperties vnpayProperties;

    @PostConstruct
    public void checkConfig() {
        log.info("=== VNPay Configuration ===");
        log.info("TMN Code: {}", vnpayProperties.getTmnCode());
        log.info("Secret Key: {}", vnpayProperties.getSecretKey());
        log.info("Pay URL: {}", vnpayProperties.getPayUrl());
        log.info("Return URL: {}", vnpayProperties.getReturnUrl());
        log.info("===========================");
    }

    public VnpayResponse createPayment(VnpayRequest request) {
        try {
            if (request.getAmount() == null || request.getAmount().isEmpty()) {
                throw new IllegalArgumentException("Số tiền không hợp lệ");
            }

            // Tạo mã giao dịch duy nhất
            String transactionId = request.getId() != null ? request.getId() : UUID.randomUUID().toString();

            // Thiết lập múi giờ VN
            TimeZone tz = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            dateFormat.setTimeZone(tz);

            String createDate = dateFormat.format(new Date());
            Calendar expireTime = Calendar.getInstance(tz);
            expireTime.add(Calendar.MINUTE, 15);
            String expireDate = dateFormat.format(expireTime.getTime());

            // Các tham số gửi lên VNPay
            Map<String, String> vnpParams = new TreeMap<>();
            vnpParams.put("vnp_Version", "2.1.0");
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", vnpayProperties.getTmnCode());
            vnpParams.put("vnp_Amount", String.valueOf(Integer.parseInt(request.getAmount()) * 100));
            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_TxnRef", transactionId);
            vnpParams.put("vnp_OrderInfo", "Thanh toan don hang: " + transactionId);
            vnpParams.put("vnp_OrderType", "other");
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_IpAddr", "127.0.0.1");
            vnpParams.put("vnp_CreateDate", createDate);
            vnpParams.put("vnp_ExpireDate", expireDate);

            // FIX: Dùng returnUrl từ config thay vì hardcode
            vnpParams.put("vnp_ReturnUrl", vnpayProperties.getReturnUrl());

            // Hash dữ liệu theo chuẩn VNPay
            String hashData = buildHashData(vnpParams);
            String secureHash = VnpayConfig.hmacSHA512(vnpayProperties.getSecretKey(), hashData);

            String queryUrl = buildQueryUrl(vnpParams);
            String paymentUrl = vnpayProperties.getPayUrl() + "?" + queryUrl + "&vnp_SecureHash=" + secureHash;

            log.info("Payment URL created for transaction [{}]", transactionId);
            log.info("CreateDate: {}, ExpireDate: {}", createDate, expireDate);

            return VnpayResponse.builder()
                    .paymentUrl(paymentUrl)
                    .transactionId(transactionId)
                    .message("Tạo thanh toán thành công")
                    .build();

        } catch (Exception e) {
            log.error("❌ Error creating payment", e);
            throw new RuntimeException("Không thể tạo thanh toán: " + e.getMessage());
        }
    }

    public boolean verifyPaymentSignature(Map<String, String> params) {
        try {
            String receivedHash = params.get("vnp_SecureHash");
            if (receivedHash == null || receivedHash.isEmpty()) {
                log.error("vnp_SecureHash is missing");
                return false;
            }

            Map<String, String> paramsToHash = new TreeMap<>(params);
            paramsToHash.remove("vnp_SecureHash");
            paramsToHash.remove("vnp_SecureHashType");

            String hashData = buildHashData(paramsToHash);
            String calculatedHash = VnpayConfig.hmacSHA512(vnpayProperties.getSecretKey(), hashData);

            boolean isValid = receivedHash.equalsIgnoreCase(calculatedHash);
            if (isValid) {
                log.info("Signature verification SUCCESS");
            } else {
                log.error("Signature verification FAILED");
                log.error("Received hash: {}", receivedHash);
                log.error("Calculated hash: {}", calculatedHash);
                log.error("Hash data: {}", hashData);
            }

            return isValid;
        } catch (Exception e) {
            log.error("Error verifying signature", e);
            return false;
        }
    }

    private String buildHashData(Map<String, String> params) throws UnsupportedEncodingException {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            if (value != null && !value.isEmpty()) {
                sb.append(key).append("=")
                        .append(URLEncoder.encode(value, StandardCharsets.UTF_8.toString()));
                if (i < keys.size() - 1) sb.append("&");
            }
        }
        return sb.toString();
    }

    private String buildQueryUrl(Map<String, String> params) throws UnsupportedEncodingException {
        List<String> keys = new ArrayList<>(params.keySet());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            if (value != null && !value.isEmpty()) {
                sb.append(URLEncoder.encode(key, StandardCharsets.UTF_8.toString()))
                        .append("=")
                        .append(URLEncoder.encode(value, StandardCharsets.UTF_8.toString()));
                if (i < keys.size() - 1) sb.append("&");
            }
        }
        return sb.toString();
    }
}