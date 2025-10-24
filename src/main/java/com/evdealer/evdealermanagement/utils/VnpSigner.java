package com.evdealer.evdealermanagement.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.evdealer.evdealermanagement.configurations.VnpayConfig;

public final class VnpSigner {

    private VnpSigner() {}

    public static String buildDataToHash(Map<String, String> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            String v = params.get(k);
            if (v == null || v.isEmpty()) continue;

            sb.append(URLEncode(k)).append('=').append(URLEncode(v));
            if (i < keys.size() - 1)
                sb.append('&');
        }
        int n = sb.length();
        if (n > 0 && sb.charAt(n - 1) == '&')
            sb.setLength(n - 1);
        return sb.toString();
    }

    public static String sign(Map<String, String> params, String secretKey) {
        String data = buildDataToHash(params);
        return VnpayConfig.hmacSHA512(secretKey, data);
    }

    public static boolean verify(Map<String, String> allParams, String secretKey) {
        Map<String, String> toSign = new HashMap<>(allParams);
        String received = toSign.remove("vnp_SecureHash");
        toSign.remove("vnp_SecureHashType");
        if (received == null)
            return false;

        String calc = sign(toSign, secretKey);
        return received.equalsIgnoreCase(calc);
    }

    private static String URLEncode(String s) {
        try {
            return URLEncoder.encode(s, StandardCharsets.US_ASCII.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
