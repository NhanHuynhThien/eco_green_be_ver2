package com.evdealer.evdealermanagement.service.contract;

import com.evdealer.evdealermanagement.dto.account.login.AccountLoginRequest;
import com.evdealer.evdealermanagement.dto.account.login.AccountLoginResponse;

public interface IAuthService {
    AccountLoginResponse login (AccountLoginRequest request);
}
