package com.sbpb.ddobak.server.domain.auth.exception;

import com.sbpb.ddobak.server.common.exception.BusinessException;
import com.sbpb.ddobak.server.common.exception.ErrorCode;

/**
 * 토큰 관련 예외 클래스
 */
public class TokenException extends BusinessException {
    
    public TokenException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public TokenException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    /**
     * 유효하지 않은 토큰 예외
     */
    public static TokenException invalidToken() {
        return new TokenException(AuthErrorCode.INVALID_TOKEN);
    }
    
    /**
     * 만료된 토큰 예외
     */
    public static TokenException expiredToken() {
        return new TokenException(AuthErrorCode.EXPIRED_TOKEN);
    }
    
    /**
     * 토큰 재사용 감지 예외
     */
    public static TokenException tokenReused() {
        return new TokenException(AuthErrorCode.TOKEN_REUSED);
    }
    
    /**
     * 절대 만료 기간 초과 예외
     */
    public static TokenException absolutelyExpired() {
        return new TokenException(AuthErrorCode.ABSOLUTE_EXPIRY_EXCEEDED);
    }
    
    /**
     * 유효하지 않은 리프레시 토큰 예외
     */
    public static TokenException invalidRefreshToken() {
        return new TokenException(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }
}
