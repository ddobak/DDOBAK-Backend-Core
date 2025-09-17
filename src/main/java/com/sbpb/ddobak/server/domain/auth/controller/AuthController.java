package com.sbpb.ddobak.server.domain.auth.controller;

import com.sbpb.ddobak.server.common.response.ApiResponse;
import com.sbpb.ddobak.server.common.response.SuccessCode;
import com.sbpb.ddobak.server.domain.auth.dto.AppleLoginRequest;
import com.sbpb.ddobak.server.domain.auth.dto.AppleTokenVerificationResponse;
import com.sbpb.ddobak.server.domain.auth.dto.AuthResponse;
import com.sbpb.ddobak.server.domain.auth.service.AuthService;
import com.sbpb.ddobak.server.domain.auth.service.JwtService;
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
    private final JwtService jwtService;
    
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
            ApiResponse.success(response, SuccessCode.SUCCESS)
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
            ApiResponse.success(response, SuccessCode.SUCCESS)
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
            ApiResponse.success(SuccessCode.SUCCESS)
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
        log.info("Token validation request received");
        
        try {
            String accessToken = authHeader.startsWith("Bearer ") 
                ? authHeader.substring(7) 
                : authHeader;
            
            // 실제 JWT 토큰 검증
            boolean isValid = jwtService.isTokenValid(accessToken);
            
            if (isValid) {
                Long userId = jwtService.getUserIdFromToken(accessToken);
                String email = jwtService.getEmailFromToken(accessToken);
                log.info("Token validation successful for user: {} ({})", email, userId);
            } else {
                log.warn("Token validation failed: invalid token");
            }
            
            return ResponseEntity.ok(
                ApiResponse.success(isValid, SuccessCode.SUCCESS)
            );
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return ResponseEntity.ok(
                ApiResponse.success(false, SuccessCode.SUCCESS)
            );
        }
    }
    
    /**
     * Apple Identity Token 검증 (테스트용)
     * 애플로그인 연동 테스트를 위함.
     * 토큰 검증만 수행하고 실제 사용자 생성이나 로그인은 하지 않음.
     * @param request Apple 로그인 요청 (Identity Token 포함)
     * @return 토큰 검증 결과와 토큰에서 추출한 정보
     */
    @PostMapping("/apple/verify")
    public ResponseEntity<ApiResponse<AppleTokenVerificationResponse>> verifyAppleToken(
        @Valid @RequestBody AppleLoginRequest request
    ) {
        log.info("Apple token verification request received for testing");
        
        AppleTokenVerificationResponse response = authService.verifyAppleToken(request);
        
        if (response.isValid()) {
            log.info("Apple token verification successful for user: {} ({})", 
                response.getEmail(), response.getUserId());
        } else {
            log.warn("Apple token verification failed: {}", response.getErrorMessage());
        }
        
        return ResponseEntity.ok(
            ApiResponse.success(response, SuccessCode.SUCCESS)
        );
    }
} 