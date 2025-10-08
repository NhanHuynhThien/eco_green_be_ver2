package com.evdealer.evdealermanagement.dto.account.custom;

import com.evdealer.evdealermanagement.entity.account.Account;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomAccountDetails implements UserDetails {

    private final Account account;

    public CustomAccountDetails(Account account) {
        this.account = account;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole()));
    }

    public Long getId() {
        return Long.valueOf(account.getId());
    }

    public String getAccountId() {
        return account.getId();
    }

    public Account getAccount() { // Thêm phương thức getAccount
        return account;
    }

    @Override
    public String getPassword() {
        return account.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return account.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return Account.Status.ACTIVE.equals(account.getStatus());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Replace 'getEmailVerified()' with the correct method or property from Account, e.g., 'isEmailVerified()'
        return true; // Assuming all accounts are enabled for simplicity
    }
}