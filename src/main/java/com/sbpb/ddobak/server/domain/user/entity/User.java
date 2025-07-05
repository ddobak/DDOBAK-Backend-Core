package com.sbpb.ddobak.server.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 엔티티
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    // OAuth 관련 필드
    @Column(name = "oauth_provider")
    private String oauthProvider;  // apple

    @Column(name = "oauth_provider_id")
    private String oauthProviderId;  // OAuth 제공자에서의 사용자 ID

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    /**
     * 사용자 상태 Enum
     */
    public enum UserStatus {
        ACTIVE,    // 활성 상태
        DELETED    // 삭제된 상태
    }

    @Builder
    public User(String email, String name,
                String oauthProvider, String oauthProviderId,
                LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime lastLoginAt, Boolean isDeleted) {
        this.email = email;
        this.name = name;
        this.oauthProvider = oauthProvider;
        this.oauthProviderId = oauthProviderId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLoginAt = lastLoginAt;
        this.isDeleted = isDeleted;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 현재 사용자 상태 반환
     * Apple 로그인 사용자는 모두 ACTIVE, 삭제된 경우만 DELETED
     */
    public UserStatus getStatus() {
        if (Boolean.TRUE.equals(isDeleted)) {
            return UserStatus.DELETED;
        }
        return UserStatus.ACTIVE;  // Apple 로그인 사용자는 모두 ACTIVE
    }

    /**
     * 마지막 로그인 시간 업데이트
     */
    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 프로필 정보 업데이트
     */
    public void updateProfile(String name) {
        if (name != null) {
            this.name = name;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 사용자 삭제 처리 (소프트 삭제)
     */
    public void delete() {
        this.isDeleted = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 이메일 업데이트
     */
    public void updateEmail(String email) {
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }
}