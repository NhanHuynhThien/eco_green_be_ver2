package com.evdealer.evdealermanagement.service.contract;

import com.evdealer.evdealermanagement.dto.account.profile.AccountProfileResponse;
import com.evdealer.evdealermanagement.dto.account.profile.AccountUpdateRequest;

public interface IAccountService {

    // Xem profile
    AccountProfileResponse getProfile(String username);

    // Cập nhật profile
    AccountProfileResponse updateProfile(String userId, AccountUpdateRequest request);

    // Xóa account
    void deleteAccount(String userId);
}
