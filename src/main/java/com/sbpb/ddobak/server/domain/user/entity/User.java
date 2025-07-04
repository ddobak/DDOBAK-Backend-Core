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

    /**
     * 사용자 상태 enum
     */
    public enum UserStatus {
        ACTIVE,     // 활성 상태
        INACTIVE,   // 비활성 상태
        SUSPENDED,  // 정지 상태
        DELETED     // 삭제 상태
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "nickname", nullable = true)
    private String nickname;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    // OAuth 관련 필드
    @Column(name = "oauth_provider")
    private String oauthProvider;  // apple, google, kakao 등

    @Column(name = "oauth_provider_id")
    private String oauthProviderId;  // OAuth 제공자에서의 사용자 ID

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;



    @Builder
    public User(String email, String name, String nickname, String profileImageUrl,
                UserStatus status, String oauthProvider, String oauthProviderId, Boolean emailVerified,
                LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime lastLoginAt, Boolean isDeleted) {
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.status = status;
        this.oauthProvider = oauthProvider;
        this.oauthProviderId = oauthProviderId;
        this.emailVerified = emailVerified;
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
        if (emailVerified == null) {
            emailVerified = false;
        }
        if (status == null) {
            status = UserStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 현재 사용자 상태 반환
     * isDeleted와 emailVerified 상태를 기반으로 계산
     */
    public UserStatus getStatus() {
        if (Boolean.TRUE.equals(isDeleted)) {
            return UserStatus.DELETED;
        }
        if (Boolean.TRUE.equals(emailVerified)) {
            return UserStatus.ACTIVE;
        }
        return UserStatus.INACTIVE;
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
    public void updateProfile(String name, String nickname, String profileImageUrl) {
        if (name != null) {
            this.name = name;
        }
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 사용자 삭제 처리 (소프트 삭제)
     */
    public void delete() {
        this.isDeleted = true;
        this.status = UserStatus.DELETED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 사용자 상태 업데이트
     */
    public void updateStatus(UserStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 이메일 업데이트
     */
    public void updateEmail(String email) {
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 이메일 검증 완료 처리
     */
    public void verifyEmail() {
        this.emailVerified = true;
        this.updatedAt = LocalDateTime.now();
    }
}