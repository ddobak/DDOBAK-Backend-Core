package com.sbpb.ddobak.server.domain.auth.service;

import java.time.Instant;

/**
 * 토큰 관리 서비스 인터페이스
 * 리프레시 토큰의 저장, 검증, 무효화 등을 담당
 */
public interface TokenService {
    
    /**
     * 리프레시 토큰 저장
     * @param userId 사용자 ID
     * @param refreshToken 리프레시 토큰
     * @param expirationTimeMillis 만료 시간 (밀리초)
     */
    void saveRefreshToken(Long userId, String refreshToken, long expirationTimeMillis);
    
    /**
     * 사용자의 리프레시 토큰 조회
     * @param userId 사용자 ID
     * @return 리프레시 토큰 (없으면 null)
     */
    String getRefreshToken(Long userId);
    
    /**
     * 리프레시 토큰 무효화
     * @param userId 사용자 ID
     */
    void invalidateRefreshToken(Long userId);
    
    /**
     * 토큰 재사용 감지
     * 보안을 위해 저장된 토큰과 제공된 토큰이 다른지 확인
     * @param userId 사용자 ID
     * @param refreshToken 검증할 리프레시 토큰
     * @return 토큰 재사용 여부
     */
    boolean isTokenReused(Long userId, String refreshToken);
    
    /**
     * 절대 만료 시간 설정
     * 토큰 재발급 횟수와 상관없이 일정 기간 후 재로그인 필요
     * @param userId 사용자 ID
     * @param expiryTimeMillis 절대 만료 시간 (밀리초)
     */
    void setAbsoluteExpiry(Long userId, long expiryTimeMillis);
    
    /**
     * 절대 만료 여부 확인
     * @param userId 사용자 ID
     * @return 절대 만료 여부
     */
    boolean isAbsolutelyExpired(Long userId);
    
    /**
     * 토큰 만료 시간 조회
     * @param userId 사용자 ID
     * @return 만료 시간
     */
    Instant getTokenExpiryDate(Long userId);
}
