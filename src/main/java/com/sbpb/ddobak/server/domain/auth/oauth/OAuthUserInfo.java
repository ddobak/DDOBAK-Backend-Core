package com.sbpb.ddobak.server.domain.auth.oauth;

import lombok.Builder;
import lombok.Getter;

/**
 * OAuth 제공자로부터 받은 사용자 정보
 * 각 OAuth 제공자별로 다른 형태의 사용자 정보를 통일된 형태로 변환
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
     * 프로필 이미지 URL (선택사항)
     */
    private final String profileImageUrl;
    
    /**
     * OAuth 제공자
     */
    private final OAuthProvider provider;
    
    /**
     * 이메일 검증 여부
     * OAuth 제공자에서 이메일이 검증되었는지 여부
     */
    @Builder.Default
    private final boolean emailVerified = false;
} 