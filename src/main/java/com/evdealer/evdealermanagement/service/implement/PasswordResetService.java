package com.evdealer.evdealermanagement.service.implement;

import com.evdealer.evdealermanagement.entity.account.Account;
import com.evdealer.evdealermanagement.repository.AccountRepository;
import com.evdealer.evdealermanagement.utils.VietNamDatetime;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    @Autowired
    private AccountRepository accountRepository;
    private final SecureRandom secureRandom;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final int OTP_EXPIRATION_MINUTES = 10;


    @Transactional
    public void requestPasswordReset(String phone) {
        Account account = accountRepository.findByPhone(phone)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản với số điện thoại này"));

        if(account.getEmail() == null || account.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tài khoản chưa l;ie kết với email này");
        }

        String otp = String.format("%06d", secureRandom.nextInt(900000)+ 100000);
        LocalDateTime expityTime = VietNamDatetime.nowVietNam().plusMinutes(OTP_EXPIRATION_MINUTES);

        account.setResetPasswordOtp(otp);
        account.setResetPasswordOtpExpiry(expityTime);

        accountRepository.save(account);
        accountRepository.save(account);
        log.info("Đã tạo và lưu mã OTP cho SĐT: {}", phone);

        // ✅ THAY ĐỔI: Gửi OTP đến email của tài khoản
        emailService.sendPasswordResetOtp(account.getEmail(), account.getPhone(), otp);
    }

    @Transactional
    public void validateAndResetPassword(String phone, String otp, String newPassword) {
        Account account = accountRepository.findByPhone(phone)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản với số điện thoại này"));

        if(account.getResetPasswordOtp() == null || !account.getResetPasswordOtp().equals(otp)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã OTP không hợp lệ.");
        }

        if (account.getResetPasswordOtpExpiry() == null || account.getResetPasswordOtpExpiry().isBefore(LocalDateTime.now())) {
            account.setResetPasswordOtp(null);
            account.setResetPasswordOtpExpiry(null);
            accountRepository.save(account);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới.");
        }

        account.setPasswordHash(passwordEncoder.encode(newPassword));

        account.setResetPasswordOtp(null);
        account.setResetPasswordOtpExpiry(null);

        accountRepository.save(account);
        log.info("Đã khôi phục mật khẩu thành công cho SĐT: {}", phone);
    }
}
