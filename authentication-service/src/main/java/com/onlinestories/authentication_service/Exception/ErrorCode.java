package com.onlinestories.authentication_service.Exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    INVALID_CREDENTIALS(1001, "Invalid username or password", HttpStatus.UNAUTHORIZED),
    REFRESH_ERROR(1002, "Refresh token error", HttpStatus.UNAUTHORIZED),
    SOCIAL_LOGIN_ERROR(1003, "Social login error", HttpStatus.BAD_REQUEST),
    RESET_PASSWORD_ERROR(1004, "Reset password error", HttpStatus.BAD_REQUEST)

    ;
    private final int code;
    private final String message;
    private final HttpStatus httpStatusCode;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = (HttpStatus) httpStatusCode;
    }
}
