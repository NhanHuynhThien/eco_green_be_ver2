package com.evdealer.evdealermanagement.exceptions;

import com.evdealer.evdealermanagement.dto.account.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors (Bean Validation)
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidation(MethodArgumentNotValidException exception) {
        // Kiểm tra null safety
        FieldError fieldError = exception.getFieldError();

        if (fieldError == null) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setCode(ErrorCode.INVALID_INPUT.getCode());
            apiResponse.setMessage(ErrorCode.INVALID_INPUT.getMessage());
            return ResponseEntity.badRequest().body(apiResponse);
        }

        String enumKey = fieldError.getDefaultMessage();
        ErrorCode errorCode;

        try {
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid error code key: {}", enumKey);
            errorCode = ErrorCode.INVALID_KEY;
        }

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity
                .status(errorCode.getHttpStatus())  // ← Sửa: Dùng httpStatus từ ErrorCode
                .body(apiResponse);
    }

    /**
     * Handle custom AppException
     */
    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse> handleAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        logger.error("AppException: {} - {}", errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity
                .status(errorCode.getHttpStatus())  // ← Sửa: Dùng httpStatus thay vì badRequest()
                .body(apiResponse);
    }

    /**
     * Handle Access Denied (403) - Từ Spring Security
     */
    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDeniedException(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.FORBIDDEN;
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        logger.warn("Access denied: {}", exception.getMessage());

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(apiResponse);
    }

    /**
     * Handle NullPointerException
     */
    @ExceptionHandler(value = NullPointerException.class)
    public ResponseEntity<ApiResponse> handleNullPointerException(NullPointerException exception) {
        logger.error("NullPointerException: ", exception);

        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage("A required value was missing");

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(apiResponse);
    }

    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
        ErrorCode errorCode = ErrorCode.INVALID_INPUT;
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(exception.getMessage() != null ? exception.getMessage() : errorCode.getMessage());

        logger.warn("Illegal argument: {}", exception.getMessage());

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(apiResponse);
    }

    /**
     * Handle all other unhandled exceptions
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse> handleGenericException(Exception exception) {
        logger.error("Unhandled exception: ", exception);

        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(apiResponse);
    }
}