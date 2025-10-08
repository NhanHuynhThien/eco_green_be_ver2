package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.dto.account.custom.CustomAccountDetails;
import com.evdealer.evdealermanagement.dto.account.login.AccountLoginResponse;
import com.evdealer.evdealermanagement.dto.account.register.AccountRegisterRequest;
import com.evdealer.evdealermanagement.dto.account.register.AccountRegisterResponse;
import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.exceptions.AppException;
import com.evdealer.evdealermanagement.exceptions.ErrorCode;
import com.evdealer.evdealermanagement.repository.AccountRepository;
import com.evdealer.evdealermanagement.utils.Utils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final JwtService jwtService;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(JwtService jwtService,
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ======================= LOGIN =======================
    public AccountLoginResponse login(String phone, String password) {

        String username = accountRepository.findUsernameByPhone(phone);
        // Step 1: Find account by username
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS, "Username does not exist"));

        // Step 2: Validate password
        if (!passwordEncoder.matches(password, account.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS, "Invalid password");
        }

         //Step 3: Validate status
        if (!Account.Status.ACTIVE.equals(account.getStatus())) {
            throw new AppException(ErrorCode.ACCOUNT_INACTIVE, "Account is not active");
        }

        // Step 4: Generate token
        String token = jwtService.generateToken(new CustomAccountDetails(account));

        return AccountLoginResponse.builder()
                .email(account.getEmail())
                .fullName(account.getFullName())
                .phone(account.getPhone())
                .dateOfBirth(account.getDateOfBirth())
                .gender(account.getGender())
                .role(account.getRole())
                .status(account.getStatus())
                .nationalId(account.getNationalId())
                .taxCode(account.getTaxCode())
                .createdAt(account.getCreatedAt())
                .updateAt(account.getUpdatedAt())
                .address(account.getAddress())
                .avatarUrl(account.getAvatarUrl())
                .token(token)
                .build();
    }

    // ======================= REGISTER =======================
    public AccountRegisterResponse register(AccountRegisterRequest request) {
        // Validate
        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS, "Email already exists");
        }
        if (request.getPhone() != null && accountRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS, "Phone number already exists");
        }
        if (request.getEmail() != null && !Utils.isValidEmail(request.getEmail())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS, "Invalid email format");
        }

        // Hash password
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        //Generate username
        String username = Utils.generateUsername(request.getPhone(), request.getFullName());

        // Create account
        Account account = Account.builder()
                .username(username)
                .email(request.getEmail())
                .phone(request.getPhone())
                .fullName(request.getFullName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .role(Account.Role.MEMBER)
                .status(Account.Status.ACTIVE)
                .passwordHash(hashedPassword)
                .build();

        Account saved = accountRepository.save(account);

        return AccountRegisterResponse.builder()
                .username(saved.getUsername())
                .email(saved.getEmail())
                .phone(saved.getPhone())
                .fullName(saved.getFullName())
                .dateOfBirth(saved.getDateOfBirth())
                .gender(saved.getGender())
                .role(saved.getRole())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .updateAt(account.getUpdatedAt())
                .address(saved.getAddress())
                .build();
    }

    // ======================= DELETE USER =======================
    public void deleteUserById(String id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND, "User with id " + id + " not found"));
        accountRepository.delete(account);
    }

    public void deleteUserByUsername(String username) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND,
                        "User with username " + username + " not found"));
        accountRepository.delete(account);
    }


}
