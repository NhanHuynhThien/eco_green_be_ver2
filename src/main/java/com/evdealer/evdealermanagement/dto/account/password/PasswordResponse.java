package com.evdealer.evdealermanagement.dto.account.password;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PasswordResponse {
    private boolean success;
    private String message;
}
