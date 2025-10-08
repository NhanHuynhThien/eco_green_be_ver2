package com.evdealer.evdealermanagement.dto.account.custom;

import com.evdealer.evdealermanagement.entity.account.Account;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomAccountDetails implements UserDetails {

    private final Account account;

    public CustomAccountDetails(Account account) {
        this.account = account;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // FIX: Thêm .name() để convert enum thành String
        return List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().name()));
    }

    public Long getId() {
        return Long.valueOf(account.getId());
    }

    public String getAccountId() {
        return account.getId();
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
        return Account.Status.ACTIVE.equals(account.getStatus());
    }
}