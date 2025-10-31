package com.evdealer.evdealermanagement.dto.password;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PasswordResetRequestDTO {
    @NotBlank
    //@Pattern()
    private String phone;
}
