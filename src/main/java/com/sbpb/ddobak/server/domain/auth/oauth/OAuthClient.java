package com.sbpb.ddobak.server.domain.auth.oauth;

/**
 * OAuth 클라이언트 공통 인터페이스
 * 각 OAuth 제공자별 클라이언트가 구현해야 하는 메서드를 정의
 */
public interface OAuthClient {
    
    /**
     * OAuth 제공자 이름 반환
     * @return 제공자 이름 (예: "apple", "google", "kakao")
     */
    String getProviderName();
    
    /**
     * OAuth 토큰을 사용하여 사용자 정보 조회
     * @param token OAuth 토큰 (Identity Token, Access Token 등)
     * @return 표준화된 사용자 정보
     */
    OAuthUserInfo getUserInfo(String token);
    
    /**
     * 해당 OAuth 제공자를 지원하는지 확인
     * @param provider OAuth 제공자
     * @return 지원 여부
     */
    boolean supports(OAuthProvider provider);
} 