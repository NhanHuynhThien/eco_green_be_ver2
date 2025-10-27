package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.repository.AccountRepository;
import com.evdealer.evdealermanagement.service.contract.IUserContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserContextService implements IUserContextService {

    private final AccountRepository accountRepository;

    @Override
    public Optional<String> getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            log.warn("No authenticated user found in SecurityContext");
            return Optional.empty();
        }

        String username = auth.getName();
        log.debug("Authenticated principal: {}", username);
        return Optional.ofNullable(username);
    }

    @Override
    public Optional<String> getCurrentUserId() {
        return getCurrentUser().map(Account::getId);
    }

    @Override
    public Optional<Account> getCurrentUser() {
        return getCurrentUsername().flatMap(username -> {
            log.debug("Looking up account for principal: {}", username);

            // Ưu tiên tìm theo username
            Optional<Account> accountOpt = accountRepository.findByUsername(username);
            if (accountOpt.isPresent()) {
                log.debug("Found account by username: {}", username);
                return accountOpt;
            }

            // Nếu không thấy, thử theo email
            accountOpt = accountRepository.findByEmail(username);
            if (accountOpt.isPresent()) {
                log.debug("Found account by email: {}", username);
                return accountOpt;
            }

            // Nếu username là UUID (user id trong JWT)
            try {
                accountOpt = accountRepository.findById(username);
                if (accountOpt.isPresent()) {
                    log.debug("Found account by ID: {}", username);
                    return accountOpt;
                }
            } catch (IllegalArgumentException ignored) {
            }

            log.warn("❌ No account found for principal: {}", username);
            return Optional.empty();
        });
    }
}
