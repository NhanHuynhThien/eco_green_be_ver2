package com.evdealer.evdealermanagement.utils;


import com.evdealer.evdealermanagement.dto.account.custom.CustomAccountDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;


public class SecurityUtils {

    public static String getCurrentAccountId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.getPrincipal() instanceof CustomAccountDetails user) {
            return  user.getAccount().getId();
        }
        return null;
    }
}
