package com.sbpb.ddobak.server.domain.auth.oauth;

import lombok.Builder;
import lombok.Getter;

/**
 * OAuth 제공자로부터 받은 사용자 정보
 * Apple OAuth 제공자에서 받은 사용자 정보를 통일된 형태로 변환
 */
@Getter
@Builder
public class OAuthUserInfo {
    
    /**
     * OAuth 제공자에서의 사용자 고유 ID
     */
    private final String providerId;
    
    /**
     * 사용자 이메일 주소
     */
    private final String email;
    
    /**
     * 사용자 이름
     */
    private final String name;
    
    /**
     * OAuth 제공자
     */
    private final OAuthProvider provider;
} 