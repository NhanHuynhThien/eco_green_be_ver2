package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.account.custom.CustomAccountDetails;
import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountDetailsService implements UserDetailsService {

    @Autowired
    private final AccountRepository accountRepository;

    public AccountDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrPhone) throws UsernameNotFoundException {
        Optional<Account> accountOpt;
        if (usernameOrPhone.matches("\\d+")) { // nếu chỉ là số → phone
            accountOpt = accountRepository.findByPhone(usernameOrPhone);
        } else {
            accountOpt = accountRepository.findByUsername(usernameOrPhone);
        }

        Account account = accountOpt.orElseThrow(() -> new UsernameNotFoundException("User Not Found"));
        return new CustomAccountDetails(account);
    }

    public UserDetails loadUserByPhone(String phone) throws UsernameNotFoundException {
        Account account = accountRepository.findByPhone(phone)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));

        return new CustomAccountDetails(account);
    }
}
