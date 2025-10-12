package com.evdealer.evdealermanagement.mapper.account;

import com.evdealer.evdealermanagement.dto.account.profile.AccountProfileResponse;
import com.evdealer.evdealermanagement.dto.account.profile.AccountUpdateRequest;
import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.exceptions.AppException;
import com.evdealer.evdealermanagement.exceptions.ErrorCode;

import org.springframework.util.StringUtils;

public final class AccountMapper {

    private AccountMapper() {}

    public static void updateAccountFromRequest(AccountUpdateRequest req, Account account) {
        if (req == null || account == null)
            return;

<<<<<<< HEAD
        // Họ và tên - null hoặc chuỗi rỗng thì không cập nhật
        if (hasText(req.getFullName())) {
            account.setFullName(trimToNull(req.getFullName()));
        }

        // Điện thoại - null hoặc chuỗi rỗng thì không cập nhật
        if (hasText(req.getPhone())) {
            account.setPhone(trimToNull(req.getPhone()));
        }

        // Địa chỉ - null hoặc chuỗi rỗng thì không cập nhật
        if (hasText(req.getAddress())) {
            account.setAddress(trimToNull(req.getAddress()));
        }

        // Email - null hoặc chuỗi rỗng thì không cập nhật
        if (hasText(req.getEmail())) {
            account.setEmail(trimToNull(req.getEmail()));
        }

        // CCCD/CMND/Hộ chiếu null hoặc chuỗi rỗng thì không cập nhật
        if (hasText(req.getNationalId())) {
            account.setNationalId(trimToNull(req.getNationalId()));
        }

        // Tax code - null hoặc chuỗi rỗng thì không cập nhật
        if (req.getTaxCode() != null) {
            account.setTaxCode(trimToNull(req.getTaxCode()));
        }

        if (req.getGender() != null) {
            Account.Gender g = parseGender(req.getGender());
            if (g != null)
                account.setGender(g);
        }

        // Ngày sinh - null thì không cập nhật
        if (req.getDateOfBirth() != null) {
            account.setDateOfBirth(req.getDateOfBirth());
        }

        // Avatar — null hoặc chuỗi rỗng thì không cập nhật
        if (req.getAvatarUrl() != null) {
            account.setAvatarUrl(trimToNull(req.getAvatarUrl()));
        }

    }

    private static boolean hasText(String s) {
        return StringUtils.hasText(s);
=======
        if (StringUtils.hasText(req.getFullName())) account.setFullName(trimToNull(req.getFullName()));
        if (StringUtils.hasText(req.getPhone())) account.setPhone(trimToNull(req.getPhone()));
        if (StringUtils.hasText(req.getAddress())) account.setAddress(trimToNull(req.getAddress()));
        if (StringUtils.hasText(req.getEmail())) account.setEmail(trimToNull(req.getEmail()));
        if (StringUtils.hasText(req.getNationalId())) account.setNationalId(trimToNull(req.getNationalId()));
        if (StringUtils.hasText(req.getTaxCode())) account.setTaxCode(trimToNull(req.getTaxCode()));
        if (StringUtils.hasText(req.getAvatarUrl())) account.setAvatarUrl(trimToNull(req.getAvatarUrl()));

        if (req.getGender() != null) {
            try {
                account.setGender(Account.Gender.valueOf(req.getGender().name()));
            } catch (IllegalArgumentException ignored) {}
        }

        if (req.getDateOfBirth() != null) {
            account.setDateOfBirth(req.getDateOfBirth());
        }
>>>>>>> 1cf309f66c3668b84b4b0f70c5b3d3bf6e470289
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String trimmed = s.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static Account.Gender parseGender(String s) {
        if (s == null)
            return null;
        String v = s.trim().toUpperCase();
        switch (v) {
            case "MALE":
                return Account.Gender.MALE;
            case "FEMALE":
                return Account.Gender.FEMALE;
            case "OTHER":
                return Account.Gender.OTHER;
            default:
                throw new AppException(ErrorCode.BAD_REQUEST, "Invalid gender");
        }
    }

    public static AccountProfileResponse mapToAccountProfileResponse(Account account) {
        if (account == null) return null;

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