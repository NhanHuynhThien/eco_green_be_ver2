package com.evdealer.evdealermanagement.service.contract;

import java.util.Optional;

public interface IUserContextService {
    Optional<String> getCurrentUsername(); // từ SecurityContext

    Optional<String> getCurrentUserId(); // map username -> account.id
}