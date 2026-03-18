package com.onlinestories.user_service.Exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND(1001, "User not found", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED_EXCEPTION(1002, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    USER_NAME_INVALID(1003,"Username must be at least 6 characters",HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1004,"Password must be at least 6 characters",HttpStatus.BAD_REQUEST),
    USER_EXISTED(1005,"User already exists",HttpStatus.BAD_REQUEST),
    USER_CREATION_FAILED(1006,"User creation failed",HttpStatus.INTERNAL_SERVER_ERROR),
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
