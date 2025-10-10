package com.evdealer.evdealermanagement.utils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MomoSignature {

    @Value("${momo.secret-key:K951B6PE1waDMi640xX08PD3vg6EkVlz}")
    private String secretKey;

    // build raw signature từ params MoMo trả về (đúng thứ tự docs)
    public String buildReturnRawSignature(Map<String, String> p) {
        // CHÚ Ý: mọi key có thể không có, nên dùng getOrDefault để tránh NPE
        return "accessKey=" + nv(p, "accessKey")
                + "&amount=" + nv(p, "amount")
                + "&extraData=" + nv(p, "extraData")
                + "&message=" + nv(p, "message")
                + "&orderId=" + nv(p, "orderId")
                + "&orderInfo=" + nv(p, "orderInfo")
                + "&orderType=" + nv(p, "orderType")
                + "&partnerCode=" + nv(p, "partnerCode")
                + "&payType=" + nv(p, "payType")
                + "&requestId=" + nv(p, "requestId")
                + "&responseTime=" + nv(p, "responseTime")
                + "&resultCode=" + nv(p, "resultCode")
                + "&transId=" + nv(p, "transId");
    }

    public boolean verify(Map<String, String> params) {
        // MoMo trả "signature" (hex lowercase)
        String receivedSig = params.get("signature");
        if (receivedSig == null)
            return false;
        String raw = buildReturnRawSignature(params);
        String calc = hmacSHA256(raw, secretKey);
        return receivedSig.equalsIgnoreCase(calc);
    }

    private static String nv(Map<String, String> m, String k) {
        return m.getOrDefault(k, "");
    }

    private static String hmacSHA256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] h = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(h.length * 2);
            for (byte b : h)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
