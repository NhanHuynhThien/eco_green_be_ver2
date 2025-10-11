package com.evdealer.evdealermanagement.mapper.account;

import com.evdealer.evdealermanagement.dto.account.profile.AccountProfileResponse;
import com.evdealer.evdealermanagement.dto.account.profile.AccountUpdateRequest;
import com.evdealer.evdealermanagement.entity.account.Account;
import org.springframework.util.StringUtils;

public final class AccountMapper {

    private AccountMapper() {
    }

    public static void updateAccountFromRequest(AccountUpdateRequest req, Account account) {
        if (req == null || account == null) return;

        // Họ và tên
        if (hasText(req.getFullName())) {
            account.setFullName(trimToNull(req.getFullName()));
        }

        // Điện thoại
        if (hasText(req.getPhone())) {
            account.setPhone(trimToNull(req.getPhone()));
        }

        // Địa chỉ
        if (hasText(req.getAddress())) {
            account.setAddress(trimToNull(req.getAddress()));
        }

        // Email
        if (hasText(req.getEmail())) {
            account.setEmail(trimToNull(req.getEmail()));
        }

        // CCCD/CMND/Hộ chiếu
        if (hasText(req.getNationalId())) {
            account.setNationalId(trimToNull(req.getNationalId()));
        }

        // Tax code — cho phép xóa nếu gửi rỗng
        if (req.getTaxCode() != null) {
            account.setTaxCode(trimToNull(req.getTaxCode()));
        }

        // Giới tính
        if (req.getGender() != null) {
            account.setGender(Account.Gender.valueOf(req.getGender().name()));
        }

        // Ngày sinh
        if (req.getDateOfBirth() != null) {
            account.setDateOfBirth(req.getDateOfBirth());
        }

        // Avatar — cho phép xóa nếu null
        if (req.getAvatarUrl() != null) {
            account.setAvatarUrl(trimToNull(req.getAvatarUrl()));
        }
    }

    private static boolean hasText(String s) {
        return StringUtils.hasText(s);
    }

    private static String trimToNull(String s) {
        return s == null ? null : s.trim();
    }

    public static AccountProfileResponse mapToAccountProfileResponse(Account account) {
        return AccountProfileResponse.builder()
                .id(account.getId())
                .username(account.getUsername())
                .email(account.getEmail())
                .fullName(account.getFullName())
                .phone(account.getPhone())
                .status(account.getStatus())
                .dateOfBirth(account.getDateOfBirth())
                .address(account.getAddress())
                .nationalId(account.getNationalId())
                .taxCode(account.getTaxCode())
                .avatarUrl(account.getAvatarUrl())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .gender(account.getGender())
                .build();
    }
}
