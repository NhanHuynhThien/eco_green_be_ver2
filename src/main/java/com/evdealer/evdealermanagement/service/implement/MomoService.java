package com.evdealer.evdealermanagement.service.implement;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.evdealer.evdealermanagement.configurations.momo.MomoProperties;
import com.evdealer.evdealermanagement.dto.payment.MomoRequest;
import com.evdealer.evdealermanagement.dto.payment.MomoResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MomoService {

    private final MomoProperties momoProperties;

    public MomoResponse createPaymentRequest(MomoRequest paymentRequest) {
        try {

            log.info("🔧 Creating MoMo payment with partnerCode={}", momoProperties.getPartnerCode());

            String partnerCode = momoProperties.getPartnerCode();
            String accessKey = momoProperties.getAccessKey();
            String secretKey = momoProperties.getSecretKey();
            String redirectUrl = momoProperties.getRedirectUrl();
            String ipnUrl = momoProperties.getIpnUrl();
            String requestType = momoProperties.getRequestType();
            // Validate amount là số nguyên dương
            long amt;
            try {
                amt = Long.parseLong(paymentRequest.getAmount());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Amount phải là số nguyên VND.");
            }
            if (amt <= 0)
                throw new IllegalArgumentException("Amount phải > 0.");

            // Generate requestId & orderId
            String requestId = partnerCode + new Date().getTime();
            String orderId = paymentRequest.getId();
            String orderInfo = "SN Mobile";

            // extraData = Base64(JSON) để mang productId
            String extraDataJson = "{\"productId\":\"" + paymentRequest.getId() + "\"}";
            String extraData = Base64.getEncoder()
                    .encodeToString(extraDataJson.getBytes(StandardCharsets.UTF_8));

            // Raw signature (đúng thứ tự tham số MoMo yêu cầu)
            String rawSignature = String.format(
                    "accessKey=%s&amount=%s&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                    accessKey, String.valueOf(amt), extraData, ipnUrl, orderId, orderInfo,
                    partnerCode, redirectUrl, requestId, requestType);

            // Ký HMAC SHA256
            String signature = signHmacSHA256(rawSignature, secretKey);

            // Body JSON gửi MoMo
            JSONObject requestBody = new JSONObject();
            requestBody.put("partnerCode", partnerCode);
            requestBody.put("accessKey", accessKey);
            requestBody.put("requestId", requestId);
            requestBody.put("amount", String.valueOf(amt));
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", redirectUrl);
            requestBody.put("ipnUrl", ipnUrl);
            requestBody.put("extraData", extraData);
            requestBody.put("requestType", requestType);
            requestBody.put("signature", signature);
            requestBody.put("lang", "en");

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost("https://test-payment.momo.vn/v2/gateway/api/create");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(requestBody.toString(), StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                // Parse JSON MoMo trả về -> build MomoResponse
                JSONObject res = new JSONObject(result.toString());

                Integer resultCode = res.has("resultCode") ? res.getInt("resultCode") : null;
                String message = res.optString("message", null);
                String payUrl = res.optString("payUrl", null);
                String deeplink = res.optString("deeplink", null);
                String qrCodeUrl = res.optString("qrCodeUrl", null);

                return new MomoResponse(payUrl, deeplink, qrCodeUrl, resultCode, message, orderId, requestId);
            }
        } catch (IllegalArgumentException e) {
            // Lỗi dữ liệu vào -> trả resultCode -1 + message
            return new MomoResponse(null, null, null, -1, e.getMessage(), null, null);
        } catch (Exception e) {
            e.printStackTrace();
            return new MomoResponse(null, null, null, -1,
                    "Failed to create payment request: " + e.getMessage(), null, null);
        }
    }

    // HMAC SHA256 signing method
    private static String signHmacSHA256(String data, String key) throws Exception {
        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSHA256.init(secretKey);
        byte[] hash = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}