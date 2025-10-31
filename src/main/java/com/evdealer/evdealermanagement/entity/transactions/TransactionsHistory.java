package com.evdealer.evdealermanagement.entity.transactions;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Getter
@Setter
//@RequiredArgsConstructor
public class TransactionsHistory {

    private String title;
    private String sellerName;
    private String buyerName;
    private LocalDateTime signedAt;
    private String contractUrl;
}
