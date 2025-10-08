package com.evdealer.evdealermanagement.dto.account.profile;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
public class AccountUpdateRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 120, message = "Full name must not exceed 120 characters")
    private String fullName;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Size(max = 255, message = "Avatar URL must not exceed 255 characters")
    private String avatarUrl;

    @Pattern(regexp = "^[0-9+()\\-\\s]{6,20}$", message = "Phone number format is invalid")
    private String phone;

    @Email(message = "Email format is invalid")
    private String email;

    @Size(max = 50, message = "Identity number must not exceed 50 characters")
    private String identityNumber; // CCCD/CMND/Hộ chiếu

    private Gender gender; // Enum giới tính

    private LocalDate dateOfBirth; // Ngày sinh

    @Size(max = 50, message = "Tax code must not exceed 50 characters")
    private String taxCode;

    // @Size(min = 3, max = 50, message = "Username must be between 3 and 50
    // characters")
    // private String username;

    // private Status status; // Sử dụng enum Status

    // public enum Status {
    // ACTIVE, INACTIVE
    // }

    public enum Gender {
        MALE, FEMALE, OTHER
    }
}