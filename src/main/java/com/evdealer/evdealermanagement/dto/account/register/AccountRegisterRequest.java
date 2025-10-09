package com.evdealer.evdealermanagement.dto.account.register;

import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.utils.REGREX;
import jakarta.validation.constraints.Email;
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

    @NotBlank(message = "MISSING_FULL_NAME")
    @Size(min = 4, max = 50, message = "FULLNAME_INVALID_LENGTH")
    private String fullName;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Pattern(regexp = REGREX.PASSWORD_REGEX, message = "PASSWORD_INVALID")
    private String password;

    @NotBlank(message = "PHONE_REQUIRED")
    @Pattern(regexp = REGREX.PHONE_REGEX, message = "INVALID_PHONE_FORMAT")
    private String phone;

//    @NotBlank(message = "EMAIL_REQUIRED")
//    @Email(message = "EMAIL_INVALID")
//    private String email;

    private LocalDate dateOfBirth;
    private Account.Gender gender;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}