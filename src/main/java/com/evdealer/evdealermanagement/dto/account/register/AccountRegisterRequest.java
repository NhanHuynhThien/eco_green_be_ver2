package com.evdealer.evdealermanagement.dto.account.register;

import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.utils.REGREX;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountRegisterRequest {
//    private String username;
    @NotBlank(message = "Full name is required")
    @Size(min = 4, max = 50, message = "Full name must be less than 50 characters")
    private String fullName;

    @NotBlank
    @Pattern(regexp = REGREX.PASSWORD_REGEX, message = "Invalid password")
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = REGREX.PHONE_REGEX, message = "Invalid VietNam phone number")
    private String phone;
    private String email;
    private LocalDate dateOfBirth;
    private Account.Gender gender;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
