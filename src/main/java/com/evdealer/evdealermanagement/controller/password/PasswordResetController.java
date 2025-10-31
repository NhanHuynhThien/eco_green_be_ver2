package com.evdealer.evdealermanagement.controller.password;

import com.evdealer.evdealermanagement.dto.password.PasswordResetConfirmDTO;
import com.evdealer.evdealermanagement.dto.password.PasswordResetRequestDTO;
import com.evdealer.evdealermanagement.service.implement.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/password/reset")
@Slf4j
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/request")
    public ResponseEntity<?> requestResetPassword(@Valid @RequestBody PasswordResetRequestDTO request) {
        log.info("Yêu cầu khôi phục mật khẩu cho SĐT: {}", request.getPhone());
        passwordResetService.requestPasswordReset(request.getPhone());
        return ResponseEntity.ok(Map.of("message", "Đã gửi mã OTP đến email liên kết (nếu SĐT tồn tại và có liên kết email)."));
    }

    @PostMapping
    public ResponseEntity<?> confirmAndResetPassword(@Valid @RequestBody PasswordResetConfirmDTO confirmDTO){
        log.info("Xác nhận khôi phục mật khẩu cho SĐT: {}", confirmDTO.getPhone());
        passwordResetService.validateAndResetPassword(
                confirmDTO.getPhone(),
                confirmDTO.getOtp(),
                confirmDTO.getNewPassword()
        );
        return ResponseEntity.ok(Map.of("message", "Khôi phục mật khẩu thành công."));
    }
}
