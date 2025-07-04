package com.sbpb.ddobak.server.domain.auth.controller;

import com.sbpb.ddobak.server.common.response.ApiResponse;
import com.sbpb.ddobak.server.common.response.SuccessCode;
import com.sbpb.ddobak.server.domain.auth.dto.AppleLoginRequest;
import com.sbpb.ddobak.server.domain.auth.dto.AuthResponse;
import com.sbpb.ddobak.server.domain.auth.response.AuthSuccessCode;
import com.sbpb.ddobak.server.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Apple 로그인
     * @param request Apple 로그인 요청
     * @return 인증 토큰 정보
     */
    @PostMapping("/apple/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithApple(
        @Valid @RequestBody AppleLoginRequest request
    ) {
        log.info("Apple login request received");
        
        AuthResponse response = authService.loginWithApple(request);
        
        return ResponseEntity.ok(
            ApiResponse.success(response, AuthSuccessCode.APPLE_LOGIN_SUCCESS)
        );
    }
    
    /**
     * 토큰 갱신
     * @param refreshToken 리프레시 토큰
     * @return 새로운 액세스 토큰
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
        @RequestHeader("Refresh-Token") String refreshToken
    ) {
        log.info("Token refresh request received");
        
        AuthResponse response = authService.refreshToken(refreshToken);
        
        return ResponseEntity.ok(
            ApiResponse.success(response, AuthSuccessCode.TOKEN_REFRESHED)
        );
    }
    
    /**
     * 로그아웃
     * @param accessToken 액세스 토큰
     * @return 로그아웃 결과
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
        @RequestHeader("Authorization") String authHeader
    ) {
        // Bearer 토큰에서 실제 토큰 추출
        String accessToken = authHeader.startsWith("Bearer ") 
            ? authHeader.substring(7) 
            : authHeader;
        
        log.info("Logout request received");
        
        authService.logout(accessToken);
        
        return ResponseEntity.ok(
            ApiResponse.success(AuthSuccessCode.LOGOUT_SUCCESS)
        );
    }
    
    /**
     * 토큰 검증 (개발/테스트용)
     * @param accessToken 액세스 토큰
     * @return 토큰 유효성 결과
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(
        @RequestHeader("Authorization") String authHeader
    ) {
        String accessToken = authHeader.startsWith("Bearer ") 
            ? authHeader.substring(7) 
            : authHeader;
        
        // JWT 토큰 검증 로직은 추후 JwtService에 추가
        boolean isValid = true; // 임시 구현
        
        return ResponseEntity.ok(
            ApiResponse.success(isValid, SuccessCode.SUCCESS)
        );
    }
} 