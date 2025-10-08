package com.evdealer.evdealermanagement.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Common errors
    SUCCESS(200, "OK"),
    BAD_REQUEST(400, "Bad request"),
    UNAUTHORIZED(401, "Invalid or expired token"),
    FORBIDDEN(403, "You do not have permission to access this resource"),
    USER_NOT_FOUND(404, "User not found"),
    PRODUCT_NOT_FOUND(404, "Product not found"),
    RESOURCE_NOT_FOUND(4041, "Requested resource not found"),
    INTERNAL_ERROR(500, "Internal server error"),
    SERVICE_UNAVAILABLE(503, "Service temporarily unavailable"),
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error"),

    // Login errors
    INVALID_CREDENTIALS(1001, "Invalid username or password"),
    ACCOUNT_LOCKED(1002, "Your account has been locked"),
    ACCOUNT_INACTIVE(1003, "Your account is not activated"),
    TOO_MANY_ATTEMPTS(1004, "Too many failed login attempts. Please try again later"),
    EMAIL_NOT_VERIFIED(1005, "Email not verified"),

    // Register errors
    USERNAME_ALREADY_EXISTS(1101, "Username is already taken"),
    EMAIL_ALREADY_EXISTS(1102, "Email is already registered"),
    WEAK_PASSWORD(1103, "Password does not meet security requirements"),
    PASSWORDS_DO_NOT_MATCH(1104, "Passwords do not match"),
    PASSWORD_TOO_SHORT(1105, "Password must be at least 6 characters"),
    DUPLICATE_NATIONAL_ID(1106, "National ID is already registered"),
    DUPLICATE_TAX_CODE(1107, "Tax code is already registered"),
    PHONE_ALREADY_EXISTS(1108, "Phone number is already registered"),
    INVALID_PHONE_FORMAT(1109, "Invalid phone number format"),

    // Validation errors
    INVALID_INPUT(1201, "Invalid input data"),
    MISSING_REQUIRED_FIELD(1202, "Missing required field"),
    INVALID_FORMAT(1203, "Invalid data format"),
    OUT_OF_RANGE(1204, "Value is out of allowed range"),
    MISSING_FULL_NAME(1205, "Full name is required"),

    // Security / Token
    INVALID_KEY(1301, "Invalid message key"),
    TOKEN_EXPIRED(1302, "Token has expired"),
    TOKEN_INVALID(1303, "Token is invalid"),
    TOKEN_MISSING(1304, "Token is missing"),

    // File / Upload
    FILE_TOO_LARGE(1401, "Uploaded file is too large"),
    UNSUPPORTED_FILE_TYPE(1402, "Unsupported file type"),
    FILE_UPLOAD_FAILED(1403, "File upload failed"),

    // Payment / Transaction
    PAYMENT_FAILED(1501, "Payment processing failed"),
    INSUFFICIENT_FUNDS(1502, "Insufficient balance"),
    TRANSACTION_DECLINED(1503, "Transaction was declined"),

    // Wishlist errors
    WISHLIST_NOT_FOUND(404, "Wishlist not found"),
    DUPLICATE_RESOURCE(409, "Resource already exists"),;

    private final int code;
    private final String message;
}