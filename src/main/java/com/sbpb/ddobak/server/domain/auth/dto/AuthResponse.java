package com.sbpb.ddobak.server.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 인증 성공 응답 DTO
 * 로그인 성공 시 클라이언트에게 전달하는 토큰 정보
 */
@Getter
@Builder
public class AuthResponse {
    
    /**
     * Access Token (JWT)
     * API 호출 시 사용하는 인증 토큰
     */
    private final String accessToken;
    
    /**
     * Refresh Token (JWT)
     * Access Token 갱신 시 사용하는 토큰
     */
    private final String refreshToken;
    
    /**
     * 토큰 타입 (기본값: Bearer)
     */
    @Builder.Default
    private final String tokenType = "Bearer";
    
    /**
     * Access Token 만료 시간 (초 단위)
     */
    private final long expiresIn;
    
    /**
     * 사용자 ID
     */
    private final Long userId;
    
    /**
     * 사용자 이메일
     */
    private final String email;
    
    /**
     * 최초 가입 여부
     * true: 새로 가입한 사용자, false: 기존 사용자
     */
    private final boolean isNewUser;
} 