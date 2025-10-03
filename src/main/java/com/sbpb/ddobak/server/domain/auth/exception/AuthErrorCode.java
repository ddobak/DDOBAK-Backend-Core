package com.sbpb.ddobak.server.domain.auth.exception;

import com.sbpb.ddobak.server.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 인증 관련 에러 코드 정의 (1xxx 범위)
 */
@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    // 인증 관련 에러 코드 (1100-1199)
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, 1100, "Invalid token"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, 1101, "Token has expired"),
    TOKEN_REUSED(HttpStatus.UNAUTHORIZED, 1102, "Token reuse detected"),
    ABSOLUTE_EXPIRY_EXCEEDED(HttpStatus.UNAUTHORIZED, 1103, "Absolute token lifetime exceeded"),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, 1104, "Invalid refresh token"),
    
    // OAuth 관련 에러 코드 (1200-1299)
    OAUTH_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, 1200, "OAuth authentication failed"),
    INVALID_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, 1201, "Invalid OAuth provider"),
    OAUTH_USER_INFO_RETRIEVAL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 1202, "Failed to retrieve OAuth user info");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
