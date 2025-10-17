package com.evdealer.evdealermanagement.dto.account.profile;

import com.evdealer.evdealermanagement.entity.account.Account;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountProfileResponse {
    private String id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private String avatarUrl;
    private LocalDate dateOfBirth;
    private String nationalId;
    private Account.Gender gender;
    private String taxCode;
    private Account.Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Account.Role role;
}
