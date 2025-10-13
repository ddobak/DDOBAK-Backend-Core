package com.sbpb.ddobak.server.domain.auth.service.impl;

import com.sbpb.ddobak.server.domain.auth.entity.UserToken;
import com.sbpb.ddobak.server.domain.auth.repository.UserTokenRepository;
import com.sbpb.ddobak.server.domain.auth.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * 토큰 관리 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenServiceImpl implements TokenService {

    private final UserTokenRepository userTokenRepository;

    @Override
    @Transactional
    public void saveRefreshToken(Long userId, String refreshToken, long expirationTimeMillis) {
        Instant expiryDate = Instant.now().plusMillis(expirationTimeMillis);
        
        Optional<UserToken> existingToken = userTokenRepository.findByUserId(userId);
        
        if (existingToken.isPresent()) {
            UserToken token = existingToken.get();
            token.updateRefreshToken(refreshToken, expiryDate);
            userTokenRepository.save(token);
            log.debug("Updated refresh token for user: {}", userId);
        } else {
            UserToken newToken = UserToken.builder()
                .userId(userId)
                .refreshToken(refreshToken)
                .expiryDate(expiryDate)
                .absoluteExpiryDate(Instant.now().plusMillis(30L * 24 * 60 * 60 * 1000)) // 30일 절대 만료
                .lastUsedAt(Instant.now())
                .isValid(true)
                .isMaster(false) // 기본값은 일반 토큰
                .accessTokenValidAfter(Instant.now()) // 현재 시간부터 AccessToken 유효
                .build();
            userTokenRepository.save(newToken);
            log.debug("Created new refresh token for user: {}", userId);
        }
    }

    @Override
    public String getRefreshToken(Long userId) {
        return userTokenRepository.findByUserId(userId)
            .filter(UserToken::isValid)
            .map(UserToken::getRefreshToken)
            .orElse(null);
    }

    @Override
    @Transactional
    public void invalidateRefreshToken(Long userId) {
        userTokenRepository.findByUserId(userId)
            .ifPresent(token -> {
                token.invalidateToken();
                userTokenRepository.save(token);
                log.debug("Invalidated refresh token for user: {}", userId);
            });
    }

    @Override
    public boolean isTokenReused(Long userId, String refreshToken) {
        return userTokenRepository.findByUserId(userId)
            .map(token -> token.isValid() && token.getRefreshToken() != null && !token.getRefreshToken().equals(refreshToken))
            .orElse(false);
    }

    @Override
    @Transactional
    public void setAbsoluteExpiry(Long userId, long expiryTimeMillis) {
        Instant expiryDate = Instant.now().plusMillis(expiryTimeMillis);
        
        userTokenRepository.findByUserId(userId)
            .ifPresentOrElse(
                token -> {
                    token.updateAbsoluteExpiryDate(expiryDate);
                    userTokenRepository.save(token);
                    log.debug("Updated absolute expiry date for user: {}", userId);
                },
                () -> {
                    // 토큰이 없는 경우 새로 생성하지 않음
                    log.debug("No token found for user: {} when setting absolute expiry", userId);
                }
            );
    }

    @Override
    public boolean isAbsolutelyExpired(Long userId) {
        return userTokenRepository.findByUserId(userId)
            .map(token -> Instant.now().isAfter(token.getAbsoluteExpiryDate()))
            .orElse(true); // 토큰이 없으면 만료된 것으로 간주
    }
    
    @Override
    public Instant getTokenExpiryDate(Long userId) {
        return userTokenRepository.findByUserId(userId)
            .map(UserToken::getExpiryDate)
            .orElse(null);
    }
    
    @Override
    public boolean isMasterToken(Long userId) {
        return userTokenRepository.findByUserId(userId)
            .map(UserToken::isMaster)
            .orElse(false); // 토큰이 없으면 마스터가 아님
    }
    
    @Override
    public Instant getAccessTokenValidAfter(Long userId) {
        return userTokenRepository.findByUserId(userId)
            .map(UserToken::getAccessTokenValidAfter)
            .orElse(Instant.EPOCH); // 토큰이 없으면 EPOCH(1970년) 반환
    }
    
    @Override
    @Transactional
    public void updateAccessTokenValidAfter(Long userId, Instant validAfter) {
        userTokenRepository.findByUserId(userId)
            .ifPresentOrElse(
                token -> {
                    token.updateAccessTokenValidAfter(validAfter);
                    userTokenRepository.save(token);
                    log.debug("Updated accessTokenValidAfter for user: {} to {}", userId, validAfter);
                },
                () -> {
                    log.debug("No token found for user: {} when updating accessTokenValidAfter", userId);
                }
            );
    }
}
