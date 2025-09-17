package com.sbpb.ddobak.server.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;

/**
 * Apple Identity Token 검증 결과 응답 DTO
 * 토큰 검증 테스트 API에서 사용
 */
@Getter
@Builder
public class AppleTokenVerificationResponse {
    
    /**
     * 토큰 유효성 여부
     */
    private final boolean isValid;
    
    /**
     * 사용자 고유 ID (sub 클레임)
     */
    private final String userId;
    
    /**
     * 사용자 이메일 (email 클레임)
     */
    private final String email;
    
    /**
     * 이메일 인증 여부 (email_verified 클레임)
     */
    private final Boolean emailVerified;
    
    /**
     * 토큰 발급자 (iss 클레임)
     */
    private final String issuer;
    
    /**
     * 토큰 대상자 (aud 클레임)
     */
    private final String audience;
    
    /**
     * 토큰 발급 시간 (iat 클레임)
     */
    private final Date issuedAt;
    
    /**
     * 토큰 만료 시간 (exp 클레임)
     */
    private final Date expiresAt;
    
    /**
     * 토큰 검증 시간
     */
    private final Date verifiedAt;
    
    /**
     * 에러 메시지 (검증 실패 시)
     */
    private final String errorMessage;
}
