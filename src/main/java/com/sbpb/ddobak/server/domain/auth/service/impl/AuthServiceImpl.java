package com.sbpb.ddobak.server.domain.auth.service.impl;

import com.sbpb.ddobak.server.domain.auth.dto.AppleLoginRequest;
import com.sbpb.ddobak.server.domain.auth.dto.AppleTokenVerificationResponse;
import com.sbpb.ddobak.server.domain.auth.dto.AuthResponse;
import com.sbpb.ddobak.server.domain.auth.exception.TokenException;
import com.sbpb.ddobak.server.domain.auth.oauth.AppleOAuthClient;
import com.sbpb.ddobak.server.domain.auth.oauth.OAuthUserInfo;
import com.sbpb.ddobak.server.domain.auth.service.AuthService;
import com.sbpb.ddobak.server.domain.auth.service.JwtService;
import com.sbpb.ddobak.server.domain.auth.service.TokenService;
import com.sbpb.ddobak.server.domain.user.entity.User;
import com.sbpb.ddobak.server.domain.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
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
    private final TokenService tokenService;
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
            
            // 5. 리프레시 토큰 저장
            tokenService.saveRefreshToken(
                user.getId(), 
                refreshToken, 
                jwtService.getRefreshTokenExpirationInMillis()
            );
            
            // 6. 절대 만료 기간 설정 (최초 로그인 또는 재로그인 시)
            tokenService.setAbsoluteExpiry(
                user.getId(), 
                jwtService.getAbsoluteTokenExpirationInMillis()
            );
            
            log.info("Apple login successful for user: {} ({})", user.getEmail(), user.getId());
            
            return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getAccessTokenExpirationInSeconds())
                .userId(user.getId())
                .email(user.getEmail())
                .isNewUser("Apple User".equals(user.getName())) // 이름이 기본값인 경우 신규 사용자로 간주
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
                throw TokenException.invalidRefreshToken();
            }
            
            // 2. 사용자 정보 조회
            Long userId = jwtService.getUserIdFromToken(refreshToken);
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // 3. 토큰 재사용 감지 (보안 강화)
            if (tokenService.isTokenReused(userId, refreshToken)) {
                tokenService.invalidateRefreshToken(userId);
                throw TokenException.tokenReused();
            }
            
            // 4. 절대 만료 시간 확인
            if (tokenService.isAbsolutelyExpired(userId)) {
                tokenService.invalidateRefreshToken(userId);
                throw TokenException.absolutelyExpired();
            }
            
            // 5. 새 액세스 토큰 생성 전에 무효화 기준 시간 설정
            // JWT의 iat는 초 단위 정밀도이므로 초 단위로 맞춰줌
            Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
            tokenService.updateAccessTokenValidAfter(userId, now);
            log.info("AccessToken 무효화 완료 - 기준 시간: {} for user: {} ({})", now, user.getEmail(), user.getId());
            
            // 6. 새 액세스 토큰 생성 (무효화 기준 시간 이후에 생성)
            String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
            
            // 7. 마스터 토큰 여부 확인
            boolean isMaster = tokenService.isMasterToken(userId);
            String responseRefreshToken;
            
            if (isMaster) {
                // 마스터 토큰인 경우: 기존 리프레시 토큰 유지 (토큰 순환 비활성화)
                responseRefreshToken = refreshToken;
                log.info("Master token refresh - keeping existing refresh token for user: {} ({})", 
                    user.getEmail(), user.getId());
            } else {
                // 일반 토큰인 경우: 새 리프레시 토큰 생성 (토큰 재발급)
                responseRefreshToken = jwtService.generateRefreshToken(user.getId());
                tokenService.saveRefreshToken(
                    user.getId(), 
                    responseRefreshToken, 
                    jwtService.getRefreshTokenExpirationInMillis()
                );
                log.info("Token refreshed with rotation for user: {} ({})", user.getEmail(), user.getId());
            }
            
            return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(responseRefreshToken)
                .expiresIn(jwtService.getAccessTokenExpirationInSeconds())
                .userId(user.getId())
                .email(user.getEmail())
                .isNewUser(false)
                .build();
                
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage(), e);
            if (e instanceof TokenException) {
                throw e;
            }
            throw new RuntimeException("Token refresh failed", e);
        }
    }
    
    @Override
    public void logout(String accessToken) {
        try {
            // 토큰에서 사용자 ID 추출
            Long userId = jwtService.getUserIdFromToken(accessToken);
            
            // 토큰 무효화
            tokenService.invalidateRefreshToken(userId);
            
            log.info("User logged out: {}", userId);
            
        } catch (Exception e) {
            log.warn("Logout processing failed: {}", e.getMessage());
            // 로그아웃은 실패해도 클라이언트에서 토큰을 삭제하면 됨
        }
    }
    
    @Override
    public AppleTokenVerificationResponse verifyAppleToken(AppleLoginRequest request) {
        try {
            log.info("Verifying Apple Identity Token for test purposes");
            
            // Apple Identity Token 검증 및 파싱 (AppleJwtUtils를 통해 직접 접근)
            Claims claims = appleOAuthClient.getAppleJwtUtils().parseAndValidateToken(request.getIdentityToken());
            
            // 검증 성공 응답 생성
            return AppleTokenVerificationResponse.builder()
                .isValid(true)
                .userId(claims.getSubject())
                .email(claims.get("email", String.class))
                .emailVerified(claims.get("email_verified", Boolean.class))
                .issuer(claims.getIssuer())
                .audience(claims.getAudience() != null ? claims.getAudience().toString() : null)
                .issuedAt(claims.getIssuedAt())
                .expiresAt(claims.getExpiration())
                .verifiedAt(new Date())
                .errorMessage(null)
                .build();
                
        } catch (Exception e) {
            log.error("Apple token verification failed: {}", e.getMessage(), e);
            
            // 검증 실패 응답 생성
            return AppleTokenVerificationResponse.builder()
                .isValid(false)
                .verifiedAt(new Date())
                .errorMessage(e.getMessage())
                .build();
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