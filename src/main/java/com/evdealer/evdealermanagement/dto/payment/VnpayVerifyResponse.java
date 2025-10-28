package com.evdealer.evdealermanagement.dto.payment;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VnpayVerifyResponse {
    boolean success;
    String message;

    String vnpTxnRef; //mã hóa đơn bên mình gửi lên vnpay
    String vnpTransactionNo; //mã giao dịch vnpay
    String vnpResponseCode; //00 là thành công
    BigDecimal amount;
    String bankCode;
    OffsetDateTime payDate;
    String status;
}
