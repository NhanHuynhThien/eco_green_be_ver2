package com.evdealer.evdealermanagement.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class PriceSerializer extends JsonSerializer<BigDecimal> {

    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        formatter.setGroupingUsed(true);
        formatter.setMaximumFractionDigits(0);

        gen.writeString(formatter.format(value));
    }

    public static String formatPrice(BigDecimal value) {
        if (value == null) {
            return null;
        }

        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        formatter.setGroupingUsed(true); // sử dụng dấu phân cách hàng nghìn
        formatter.setMaximumFractionDigits(0); // không có chữ số thập phân

        return formatter.format(value);
    }
}
