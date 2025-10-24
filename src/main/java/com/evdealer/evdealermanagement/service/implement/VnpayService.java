package com.evdealer.evdealermanagement.service.implement;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.evdealer.evdealermanagement.configurations.VnpayConfig;
import com.evdealer.evdealermanagement.configurations.vnpay.VnpayProperties;
import com.evdealer.evdealermanagement.dto.payment.VnpayRequest;
import com.evdealer.evdealermanagement.dto.payment.VnpayResponse;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VnpayService {

    private final VnpayProperties vnpayProperties;

    // Lưu mapping orderId -> frontend returnUrl
    // TODO: Production nên dùng Redis với TTL 30 phút
    private final Map<String, String> frontendReturnUrls = new ConcurrentHashMap<>();

    @PostConstruct
    public void checkConfig() {
        log.info("=== VNPay Configuration ===");
        log.info("TMN Code: {}", vnpayProperties.getTmnCode());
        log.info("Secret Key: {}", vnpayProperties.getSecretKey());
        log.info("Pay URL: {}", vnpayProperties.getPayUrl());
        log.info("Backend Callback URL: {}", vnpayProperties.getReturnUrl());
        log.info("===========================");
    }

    /**
     * Tạo URL thanh toán VNPay
     * Frontend gửi: { id, amount, returnUrl }
     */
    public VnpayResponse createPayment(VnpayRequest request) {
        try {
            // Validate
            if (request.getAmount() == null || request.getAmount().isEmpty()) {
                throw new IllegalArgumentException("Số tiền không hợp lệ");
            }

            String orderId = request.getId() != null && !request.getId().isEmpty()
                    ? request.getId()
                    : UUID.randomUUID().toString();

            // Tạo params cho VNPay
            String createDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

            Map<String, String> vnpParams = new TreeMap<>();
            vnpParams.put("vnp_Version", "2.1.0");
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", vnpayProperties.getTmnCode());
            vnpParams.put("vnp_Amount", String.valueOf(Integer.parseInt(request.getAmount()) * 100)); // VNPay yêu cầu x100
            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_TxnRef", orderId);
            vnpParams.put("vnp_OrderInfo", "Thanh toan don hang: " + orderId);
            vnpParams.put("vnp_OrderType", "other");
            vnpParams.put("vnp_Locale", "vn");
            String vnp_ReturnUrl = request.getReturnUrl();
            if (vnp_ReturnUrl == null || vnp_ReturnUrl.isBlank()) {
                vnp_ReturnUrl = vnpayProperties.getReturnUrl(); // fallback nếu FE không gửi
            }
            log.debug("Using VNPay return URL: {}", vnp_ReturnUrl);

            vnpParams.put("vnp_ReturnUrl", vnp_ReturnUrl);

            vnpParams.put("vnp_IpAddr", "127.0.0.1");
            vnpParams.put("vnp_CreateDate", createDate);

            // Expire sau 15 phút
            Calendar expireTime = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            expireTime.add(Calendar.MINUTE, 15);
            vnpParams.put("vnp_ExpireDate", new SimpleDateFormat("yyyyMMddHHmmss").format(expireTime.getTime()));

            // Tạo chữ ký
            String hashData = buildHashData(vnpParams);
            String secureHash = VnpayConfig.hmacSHA512(vnpayProperties.getSecretKey(), hashData);

            log.debug("Hash data: {}", hashData);
            log.debug("Secure hash: {}", secureHash);

            // Build payment URL
            String queryUrl = buildQueryUrl(vnpParams);
            String paymentUrl = vnpayProperties.getPayUrl() + "?" + queryUrl + "&vnp_SecureHash=" + secureHash;

            log.info("Payment URL created for order [{}]", orderId);

            return VnpayResponse.builder()
                    .paymentUrl(paymentUrl)
                    .transactionId(orderId)
                    .message("Tạo thanh toán thành công")
                    .build();

        } catch (Exception e) {
            log.error("Error creating payment", e);
            throw new RuntimeException("Không thể tạo thanh toán: " + e.getMessage());
        }
    }

    /**
     * Verify chữ ký từ VNPay callback
     */
    public boolean verifyPaymentSignature(Map<String, String> params) {
        try {
            String receivedHash = params.get("vnp_SecureHash");
            if (receivedHash == null || receivedHash.isEmpty()) {
                log.error("Missing vnp_SecureHash");
                return false;
            }

            Map<String, String> paramsToHash = new TreeMap<>(params);
            paramsToHash.remove("vnp_SecureHash");
            paramsToHash.remove("vnp_SecureHashType");

            String hashData = buildHashData(paramsToHash);
            String calculatedHash = VnpayConfig.hmacSHA512(vnpayProperties.getSecretKey(), hashData);

            log.debug("Received hash: {}", receivedHash);
            log.debug("Calculated hash: {}", calculatedHash);

            boolean isValid = receivedHash.equalsIgnoreCase(calculatedHash);

            if (isValid) {
                log.info("Signature verification SUCCESS");
            } else {
                log.error("Signature verification FAILED");
                log.error("Hash data: {}", hashData);
            }

            return isValid;

        } catch (Exception e) {
            log.error("❌ Error verifying signature", e);
            return false;
        }
    }

    /**
     * Lấy frontend returnUrl và xóa khỏi cache
     */
    public String getFrontendReturnUrl(String orderId) {
        String url = frontendReturnUrls.remove(orderId);
        if (url == null) {
            log.warn("⚠️ No returnUrl found for order [{}], using default", orderId);
            return "http://localhost:5173/payment/result";
        }
        log.info("📤 Retrieved returnUrl for order [{}]: {}", orderId, url);
        return url;
    }

    /**
     * Build hash data: key1=value1&key2=value2&...
     */
    private String buildHashData(Map<String, String> params) throws UnsupportedEncodingException {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();

        for (int i = 0; i < fieldNames.size(); i++) {
            String key = fieldNames.get(i);
            String value = params.get(key);

            if (value != null && !value.isEmpty()) {
                hashData.append(key)
                        .append("=")
                        .append(URLEncoder.encode(value, StandardCharsets.US_ASCII.toString()));
                if (i < fieldNames.size() - 1) {
                    hashData.append("&");
                }
            }
        }

        return hashData.toString();
    }

    /**
     * Build query URL với encoding
     */
    private String buildQueryUrl(Map<String, String> params) throws UnsupportedEncodingException {
        List<String> keys = new ArrayList<>(params.keySet());
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);

            if (value != null && !value.isEmpty()) {
                sb.append(URLEncoder.encode(key, StandardCharsets.UTF_8.toString()));
                sb.append('=');
                sb.append(URLEncoder.encode(value, StandardCharsets.UTF_8.toString()));
                if (i < keys.size() - 1) {
                    sb.append('&');
                }
            }
        }

        return sb.toString();
    }
}