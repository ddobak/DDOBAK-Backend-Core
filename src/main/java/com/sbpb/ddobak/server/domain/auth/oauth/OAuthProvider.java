package com.sbpb.ddobak.server.domain.auth.oauth;

/**
 * OAuth 제공자 열거형
 * 지원하는 소셜 로그인 서비스 목록
 */
public enum OAuthProvider {
    /**
     * Apple 로그인
     */
    APPLE("apple"),
    
    /**
     * Google 로그인
     */
    GOOGLE("google"),
    
    /**
     * 카카오 로그인
     */
    KAKAO("kakao");
    
    private final String providerName;
    
    OAuthProvider(String providerName) {
        this.providerName = providerName;
    }
    
    public String getProviderName() {
        return providerName;
    }
    
    /**
     * 문자열로부터 OAuthProvider 조회
     * @param providerName 제공자 이름
     * @return OAuthProvider 또는 null
     */
    public static OAuthProvider fromString(String providerName) {
        for (OAuthProvider provider : values()) {
            if (provider.providerName.equalsIgnoreCase(providerName)) {
                return provider;
            }
        }
        return null;
    }
} 