package com.sbpb.ddobak.server.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 사용자 토큰 엔티티
 * 리프레시 토큰 및 만료 정보 관리
 */
@Entity
@Table(name = "user_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(length = 500)
    private String refreshToken;

    private Instant expiryDate;
    
    private Instant absoluteExpiryDate;
    
    private Instant lastUsedAt;
    
    @Column(nullable = false)
    private boolean isValid;
    
    /**
     * 마스터 토큰 처리하기
     * true인 경우 리프레시 토큰 순환을 비활성화.
     * 개발/테스트 환경에서 리프레시 토큰이 계속 유지되도록 함
     */
    @Column(nullable = false)
    private boolean isMaster;
    
    /**
     * AccessToken 무효화 기준 시간
     * 이 시간 이후에 발급된 AccessToken만 유효함
     * 리프레시 토큰 갱신 시 이 값을 현재 시간으로 업데이트하여 기존 AccessToken 무효화
     */
    private Instant accessTokenValidAfter;
    
    @Builder
    public UserToken(Long userId, String refreshToken, Instant expiryDate, 
                     Instant absoluteExpiryDate, Instant lastUsedAt, boolean isValid, boolean isMaster, Instant accessTokenValidAfter) {
        this.userId = userId;
        this.refreshToken = refreshToken;
        this.expiryDate = expiryDate;
        this.absoluteExpiryDate = absoluteExpiryDate;
        this.lastUsedAt = lastUsedAt;
        this.isValid = isValid;
        this.isMaster = isMaster;
        this.accessTokenValidAfter = accessTokenValidAfter;
    }
    
    @PrePersist
    protected void onCreate() {
        if (lastUsedAt == null) {
            lastUsedAt = Instant.now();
        }
        if (isValid == false) {
            isValid = true;
        }
    }
    
    /**
     * 리프레시 토큰 업데이트
     * @param refreshToken 새 리프레시 토큰
     * @param expiryDate 만료 시간
     */
    public void updateRefreshToken(String refreshToken, Instant expiryDate) {
        this.refreshToken = refreshToken;
        this.expiryDate = expiryDate;
        this.lastUsedAt = Instant.now();
        this.isValid = true;
    }
    
    /**
     * 토큰 무효화
     * 로그아웃 또는 보안 문제 발생 시 호출
     */
    public void invalidateToken() {
        this.isValid = false;
        this.refreshToken = null;
    }
    
    /**
     * 절대 만료 시간 업데이트 🌟
     * @param absoluteExpiryDate 절대 만료 시간
     */
    public void updateAbsoluteExpiryDate(Instant absoluteExpiryDate) {
        this.absoluteExpiryDate = absoluteExpiryDate;
    }
    
    /**
     * 마스터 토큰 설정
     * @param isMaster 마스터 토큰 여부
     */
    public void setMaster(boolean isMaster) {
        this.isMaster = isMaster;
    }
    
    /**
     * AccessToken 무효화 기준 시간 업데이트
     * 리프레시 토큰 갱신 시 호출하여 기존 AccessToken 무효화
     * @param accessTokenValidAfter 새로운 기준 시간
     */
    public void updateAccessTokenValidAfter(Instant accessTokenValidAfter) {
        this.accessTokenValidAfter = accessTokenValidAfter;
    }
}
