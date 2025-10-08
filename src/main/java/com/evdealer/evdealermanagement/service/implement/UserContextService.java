package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.repository.AccountRepository;
import com.evdealer.evdealermanagement.service.contract.IUserContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserContextService implements IUserContextService {

    private final AccountRepository accountRepository;

    @Override
    public Optional<String> getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        // auth.getName() = principal username do Spring Security set khi xác thực JWT
        return Optional.ofNullable(auth.getName());
    }

    @Override
    public Optional<String> getCurrentUserId() {
        return getCurrentUsername()
                .flatMap(u -> accountRepository.findByUsername(u).map(Account::getId));
    }
}