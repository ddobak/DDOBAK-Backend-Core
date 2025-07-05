package com.sbpb.ddobak.server.domain.auth.service.impl;

import com.sbpb.ddobak.server.domain.auth.dto.AppleLoginRequest;
import com.sbpb.ddobak.server.domain.auth.dto.AuthResponse;
import com.sbpb.ddobak.server.domain.auth.oauth.AppleOAuthClient;
import com.sbpb.ddobak.server.domain.auth.oauth.OAuthUserInfo;
import com.sbpb.ddobak.server.domain.auth.service.AuthService;
import com.sbpb.ddobak.server.domain.auth.service.JwtService;
import com.sbpb.ddobak.server.domain.user.entity.User;
import com.sbpb.ddobak.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 인증 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    
    private final AppleOAuthClient appleOAuthClient;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public AuthResponse loginWithApple(AppleLoginRequest request) {
        try {
            // 1. Apple Identity Token 검증 및 사용자 정보 추출
            OAuthUserInfo oAuthUserInfo = appleOAuthClient.getUserInfo(request.getIdentityToken());
            
            // 2. 기존 사용자 조회 또는 신규 사용자 생성
            User user = findOrCreateUser(oAuthUserInfo);
            
            // 3. 로그인 시간 업데이트
            user.updateLastLoginAt();
            userRepository.save(user);
            
            // 4. JWT 토큰 생성
            String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
            String refreshToken = jwtService.generateRefreshToken(user.getId());
            
            log.info("Apple login successful for user: {} ({})", user.getEmail(), user.getId());
            
            return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getAccessTokenExpirationInSeconds())
                .userId(user.getId())
                .email(user.getEmail())
                .isNewUser(user.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(1))) // 1분 이내 생성된 경우 신규 사용자
                .build();
                
        } catch (Exception e) {
            log.error("Apple login failed: {}", e.getMessage(), e);
            throw new RuntimeException("Apple login failed", e);
        }
    }
    
    @Override
    public AuthResponse refreshToken(String refreshToken) {
        try {
            // 1. Refresh Token 검증
            if (!jwtService.isTokenValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
                throw new RuntimeException("Invalid refresh token");
            }
            
            // 2. 사용자 정보 조회
            Long userId = jwtService.getUserIdFromToken(refreshToken);
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // 3. 새로운 Access Token 생성
            String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
            
            log.info("Token refreshed for user: {} ({})", user.getEmail(), user.getId());
            
            return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // 기존 Refresh Token 재사용
                .expiresIn(jwtService.getAccessTokenExpirationInSeconds())
                .userId(user.getId())
                .email(user.getEmail())
                .isNewUser(false)
                .build();
                
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage(), e);
            throw new RuntimeException("Token refresh failed", e);
        }
    }
    
    @Override
    public void logout(String accessToken) {
        try {
            // 현재 구현에서는 단순히 로그를 남김
            // 추후 Redis 등을 사용한 토큰 블랙리스트 구현 가능
            Long userId = jwtService.getUserIdFromToken(accessToken);
            log.info("User logged out: {}", userId);
            
        } catch (Exception e) {
            log.warn("Logout processing failed: {}", e.getMessage());
            // 로그아웃은 실패해도 클라이언트에서 토큰을 삭제하면 됨
        }
    }
    
    /**
     * 기존 사용자 조회 또는 신규 사용자 생성
     * @param oAuthUserInfo OAuth로 받은 사용자 정보
     * @return 사용자 엔티티
     */
    private User findOrCreateUser(OAuthUserInfo oAuthUserInfo) {
        // Apple Provider ID로 기존 사용자 검색
        Optional<User> existingUser = userRepository.findByAppleId(oAuthUserInfo.getProviderId());
        
        if (existingUser.isPresent()) {
            // 기존 사용자 정보 업데이트 (이메일이 변경될 수 있음)
            User user = existingUser.get();
            if (!user.getEmail().equals(oAuthUserInfo.getEmail())) {
                user.updateEmail(oAuthUserInfo.getEmail());
                log.info("User email updated: {} -> {}", user.getEmail(), oAuthUserInfo.getEmail());
            }
            return user;
        } else {
            // 신규 사용자 생성
            User newUser = User.builder()
                .email(oAuthUserInfo.getEmail())
                .name(oAuthUserInfo.getName() != null ? oAuthUserInfo.getName() : "Apple User")
                .oauthProvider("apple")
                .oauthProviderId(oAuthUserInfo.getProviderId())
                .build();
            
            User savedUser = userRepository.save(newUser);
            log.info("New user created via Apple login: {} ({})", savedUser.getEmail(), savedUser.getId());
            return savedUser;
        }
    }
} 