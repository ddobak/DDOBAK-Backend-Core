package com.sbpb.ddobak.server.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Apple 로그인 요청 DTO
 * Apple Identity Token과 Authorization Code를 사용하여 로그인 처리
 */
@Getter
@Setter
@NoArgsConstructor
public class AppleLoginRequest {
    
    /**
     * Apple Identity Token (JWT 형식)
     * Apple에서 발급한 사용자 정보가 포함된 JWT 토큰
     */
    @NotBlank(message = "Identity token is required")
    private String identityToken;
    
    /**
     * Apple Authorization Code (선택사항)
     * Refresh Token 발급을 위해 필요
     * 탈퇴 시 Apple 서버에서 계정 삭제를 위해 사용됨
     */
    private String authorizationCode;
} 