package com.evdealer.evdealermanagement.service.implement;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.evdealer.evdealermanagement.dto.payment.MomoRequest;
import com.evdealer.evdealermanagement.dto.payment.MomoResponse;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class MomoService {

    private static final String PARTNER_CODE = "MOMO";
    private static final String ACCESS_KEY = "F8BBA842ECF85";
    private static final String SECRET_KEY = "K951B6PE1waDMi640xX08PD3vg6EkVlz";
    private static final String REDIRECT_URL = "http://localhost:8080/api/momo/return";
    private static final String IPN_URL = "https://callback.url/notify";
    private static final String REQUEST_TYPE = "payWithMethod";

    public MomoResponse createPaymentRequest(MomoRequest paymentRequest) {
        try {
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
            String requestId = PARTNER_CODE + new Date().getTime();
            String orderId = paymentRequest.getId();
            String orderInfo = "SN Mobile";

            // extraData = Base64(JSON) để mang productId
            String extraDataJson = "{\"productId\":" + paymentRequest.getId() + "}";
            String extraData = Base64.getEncoder()
                    .encodeToString(extraDataJson.getBytes(StandardCharsets.UTF_8));

            // Raw signature (đúng thứ tự tham số MoMo yêu cầu)
            String rawSignature = String.format(
                    "accessKey=%s&amount=%s&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                    ACCESS_KEY, String.valueOf(amt), extraData, IPN_URL, orderId, orderInfo,
                    PARTNER_CODE, REDIRECT_URL, requestId, REQUEST_TYPE);

            // Ký HMAC SHA256
            String signature = signHmacSHA256(rawSignature, SECRET_KEY);

            // Body JSON gửi MoMo
            JSONObject requestBody = new JSONObject();
            requestBody.put("partnerCode", PARTNER_CODE);
            requestBody.put("accessKey", ACCESS_KEY);
            requestBody.put("requestId", requestId);
            requestBody.put("amount", String.valueOf(amt));
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", REDIRECT_URL);
            requestBody.put("ipnUrl", IPN_URL);
            requestBody.put("extraData", extraData);
            requestBody.put("requestType", REQUEST_TYPE);
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