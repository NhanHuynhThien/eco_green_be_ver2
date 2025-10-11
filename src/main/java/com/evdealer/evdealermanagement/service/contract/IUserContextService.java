package com.evdealer.evdealermanagement.service.contract;

import java.util.Optional;

import com.evdealer.evdealermanagement.entity.account.Account;

public interface IUserContextService {
    Optional<String> getCurrentUsername(); // từ SecurityContext

    Optional<String> getCurrentUserId(); // map username -> account.id

    Optional<Account> getCurrentUser();
}