package com.sbpb.ddobak.server.domain.auth.service;

import com.sbpb.ddobak.server.domain.auth.dto.AppleLoginRequest;
import com.sbpb.ddobak.server.domain.auth.dto.AuthResponse;

/**
 * 인증 서비스 인터페이스
 * 다양한 인증 방식을 처리하는 메서드들을 정의
 */
public interface AuthService {
    
    /**
     * Apple 로그인 처리
     * @param request Apple 로그인 요청 정보
     * @return 인증 결과 (토큰 정보 포함)
     */
    AuthResponse loginWithApple(AppleLoginRequest request);
    
    /**
     * 토큰 갱신
     * @param refreshToken 리프레시 토큰
     * @return 새로운 액세스 토큰
     */
    AuthResponse refreshToken(String refreshToken);
    
    /**
     * 로그아웃
     * @param accessToken 액세스 토큰
     */
    void logout(String accessToken);
} 