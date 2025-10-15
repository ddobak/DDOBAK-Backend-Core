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
            
            // 2. 신규 사용자 여부 확인 (DB에 사용자 존재 여부로 판단)
            boolean isNewUser = !userRepository.findByAppleId(oAuthUserInfo.getProviderId()).isPresent();
            
            // 3. 기존 사용자 조회 또는 신규 사용자 생성
            User user = findOrCreateUser(oAuthUserInfo, request.getAuthorizationCode());
            
            // 4. Authorization Code가 있으면 Apple Refresh Token 발급 및 저장
            if (request.getAuthorizationCode() != null && !request.getAuthorizationCode().isEmpty()) {
                try {
                    AppleOAuthClient.AppleTokenResponse appleTokenResponse = 
                        appleOAuthClient.getTokens(request.getAuthorizationCode());
                    
                    // Apple Refresh Token 저장 (참고용, 탈퇴 시 사용 안 함)
                    user.updateAppleRefreshToken(appleTokenResponse.getRefreshToken());
                    log.info("Apple refresh token saved for user: {} ({})", user.getEmail(), user.getId());
                } catch (Exception e) {
                    // Refresh Token 발급 실패 시 경고만 (필수 아님)
                    log.warn("Failed to get Apple refresh token for user: {} ({}). Error: {}", 
                        user.getEmail(), user.getId(), e.getMessage());
                }
            }
            
            // 5. 로그인 시간 업데이트
            user.updateLastLoginAt();
            userRepository.save(user);
            
            // 6. JWT 토큰 생성
            String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
            String refreshToken = jwtService.generateRefreshToken(user.getId());
            
            // 7. 리프레시 토큰 저장
            tokenService.saveRefreshToken(
                user.getId(), 
                refreshToken, 
                jwtService.getRefreshTokenExpirationInMillis()
            );
            
            // 8. 절대 만료 기간 설정 (최초 로그인 또는 재로그인 시)
            tokenService.setAbsoluteExpiry(
                user.getId(), 
                jwtService.getAbsoluteTokenExpirationInMillis()
            );
            
            log.info("Apple login successful for user: {} ({}), isNewUser: {}", user.getEmail(), user.getId(), isNewUser);
            
            return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getAccessTokenExpirationInSeconds())
                .userId(user.getId())
                .email(user.getEmail())
                .isNewUser(isNewUser)
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
     * @param authorizationCode Apple Authorization Code
     * @return 사용자 엔티티
     */
    private User findOrCreateUser(OAuthUserInfo oAuthUserInfo, String authorizationCode) {
        // 이메일 필수 체크
        if (oAuthUserInfo.getEmail() == null) {
            log.error("Email is null. ProviderId: {}", oAuthUserInfo.getProviderId());
            throw new IllegalArgumentException(
                "Apple에서 이메일 정보를 제공하지 않았습니다. Apple 로그인을 다시 시도해주세요.");
        }
        
        // Apple Provider ID로 기존 사용자 검색 (탈퇴한 사용자는 DB에서 삭제되므로 조회 안됨)
        Optional<User> existingUser = userRepository.findByAppleId(oAuthUserInfo.getProviderId());
        
        if (existingUser.isPresent()) {
            // 기존 사용자 - 로그인
            User user = existingUser.get();
            
            // 기존 사용자 정보 업데이트 (이메일이 변경될 수 있음)
            if (!user.getEmail().equals(oAuthUserInfo.getEmail())) {
                user.updateEmail(oAuthUserInfo.getEmail());
                log.info("User email updated: {} -> {}", user.getEmail(), oAuthUserInfo.getEmail());
            }
            
            log.info("Existing user logged in: {} ({})", user.getEmail(), user.getId());
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