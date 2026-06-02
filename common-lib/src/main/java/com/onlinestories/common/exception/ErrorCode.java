package com.onlinestories.common.exception;

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
    ALREADY_CHECKED_IN(1007,"User has already checked in today",HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS(1008, "Email already exists", HttpStatus.BAD_REQUEST),
    USERNAME_ALREADY_EXISTS(1009, "Username already exists", HttpStatus.BAD_REQUEST),
    USER_NOT_VERIFIED(1010, "User email is not verified", HttpStatus.FORBIDDEN),
    EMAIL_SEND_FAILED(1011, "Failed to send email", HttpStatus.INTERNAL_SERVER_ERROR),

    STORY_NOT_FOUND(2001, "Story not found", HttpStatus.NOT_FOUND),
    CHAPTER_NOT_FOUND(2002, "Chapter not found", HttpStatus.NOT_FOUND),
    COMMENT_NOT_FOUND(2003, "Comment not found", HttpStatus.NOT_FOUND),
    DRAFT_NOT_FOUND(2004, "Draft not found", HttpStatus.NOT_FOUND),
    DRAFT_ALREADY_EXISTS(2005, "Draft already exists for this story", HttpStatus.BAD_REQUEST),
    VERSION_NOT_FOUND(2006, "Version not found", HttpStatus.NOT_FOUND),
    CHAPTER_ALREADY_PUBLISHED(2007, "Chapter is already published", HttpStatus.BAD_REQUEST),
    NOT_AUTHOR_OF_STORY(2008, "User is not the author of the story", HttpStatus.FORBIDDEN),
    GENRE_NOT_FOUND(2009, "Genre not found", HttpStatus.NOT_FOUND),
    GENRE_ALREADY_EXISTS(2010, "Genre already exists", HttpStatus.BAD_REQUEST),
    PARENT_COMMENT_NOT_FOUND(2011, "Parent comment not found", HttpStatus.NOT_FOUND),
    STORY_IS_NOT_PUBLISHED(2012, "Story is not published", HttpStatus.BAD_REQUEST),
    CHAPTER_IS_NOT_PUBLISHED(2013, "Chapter is not published", HttpStatus.BAD_REQUEST),
    RATING_ALREADY_EXISTS(2014, "User has already rated this story", HttpStatus.BAD_REQUEST),
    STORY_IS_COMPLETED(2015, "Story is already completed", HttpStatus.BAD_REQUEST),
    INVALID_FILE(2016, "File is empty or missing", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_FILE_TYPE(2017, "Only .docx files are supported", HttpStatus.BAD_REQUEST),
    EMPTY_IMPORT_CONTENT(2018, "Imported content is empty", HttpStatus.BAD_REQUEST),
    FILE_IMPORT_FAILED(2019, "Failed to import Word file", HttpStatus.INTERNAL_SERVER_ERROR),
    RATING_NOT_FOUND(2020, "Rating not found", HttpStatus.NOT_FOUND),
    FAVORITE_ALREADY_EXISTS(2021, "User has already liked this story", HttpStatus.BAD_REQUEST),
    FAVORITE_NOT_FOUND(2022, "Favorite not found", HttpStatus.NOT_FOUND),
    CHAPTER_IS_TAKEN_DOWN(2023, "Chapter is taken down", HttpStatus.BAD_REQUEST),
    GENERATE_IMAGE_ERROR(2024, "Generate image error", HttpStatus.INTERNAL_SERVER_ERROR),
    STORY_IS_NOT_PREMIUM(2025, "Story is not premium", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED(2900, "Access denied", HttpStatus.FORBIDDEN),

    INVALID_CREDENTIALS(3001, "Invalid username or password", HttpStatus.UNAUTHORIZED),
    REFRESH_ERROR(3002, "Refresh token error", HttpStatus.UNAUTHORIZED),
    SOCIAL_LOGIN_ERROR(3003, "Social login error", HttpStatus.BAD_REQUEST),
    RESET_PASSWORD_ERROR(3004, "Reset password error", HttpStatus.BAD_REQUEST),

    REPORT_NOT_FOUND(4001, "Report not found", HttpStatus.NOT_FOUND),
    REPORT_HAS_NOT_BEEN_HANDLED(4002, "Report has not been handled yet", HttpStatus.BAD_REQUEST),

    UPDATE_TOKEN_ERROR(5001, "Update token error", HttpStatus.INTERNAL_SERVER_ERROR),
    WALLET_NOT_FOUND(5002, "Wallet not found", HttpStatus.NOT_FOUND),
    INSUFFICIENT_BALANCE(5003, "Insufficient balance", HttpStatus.BAD_REQUEST),
    UNLOCK_ERROR(5004, "Unlock story error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNLOCK_ALREADY(5005, "User has already unlocked this story", HttpStatus.BAD_REQUEST),
    INVALID_READING_TOKENS(5006, "Invalid reading tokens", HttpStatus.BAD_REQUEST),
    INVALID_TOP_UP_AMOUNT(5007, "Invalid top-up amount", HttpStatus.BAD_REQUEST),
    TOP_UP_ERROR(5008, "Top-up error", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_NOT_FOUND(5009, "Payment not found", HttpStatus.NOT_FOUND),
    HISTORY_NOT_FOUND(5010, "Transaction history not found", HttpStatus.NOT_FOUND),
    HISTORY_ERROR(5011, "Transaction history error", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYOUT_ERROR(5012, "Payout error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_WITHDRAWAL_REQUEST(5013, "Invalid withdrawal request", HttpStatus.BAD_REQUEST),
    WITHDRAWAL_NOT_FOUND(5014, "Withdrawal not found", HttpStatus.NOT_FOUND),
    PAYOS_PAYOUT_NOT_CONFIGURED(5015, "payOS payout is not configured", HttpStatus.INTERNAL_SERVER_ERROR),

    PLAGIARISM_DETECTED(6001, "Plagiarism detected", HttpStatus.BAD_REQUEST),

    CREATE_CONTEST_FAILED(7001, "Failed to create contest", HttpStatus.INTERNAL_SERVER_ERROR),
    CONTEST_NOT_FOUND(7002, "Contest not found", HttpStatus.NOT_FOUND),
    CONTEST_NOT_ACTIVE(7003, "Contest is not active or has already ended", HttpStatus.BAD_REQUEST),
    ALREADY_PARTICIPATED(7004, "User has already participated in this contest", HttpStatus.BAD_REQUEST),
    PARTICIPATE_CONTEST_FAILED(7005, "Failed to participate in contest", HttpStatus.INTERNAL_SERVER_ERROR),


    GUEST_USER_FORBIDDEN(9001, "Guest users are not allowed to perform this action", HttpStatus.FORBIDDEN),
    UNCATEGORIZED_EXCEPTION(9999, "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR)
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
