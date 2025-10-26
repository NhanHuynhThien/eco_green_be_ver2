package com.evdealer.evdealermanagement.dto.account.ban;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BanRequest {
    private String reason;
}
