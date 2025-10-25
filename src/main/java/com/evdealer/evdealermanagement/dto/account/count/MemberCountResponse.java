package com.evdealer.evdealermanagement.dto.account.count;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberCountResponse {
    private String role;
    private long total;
}