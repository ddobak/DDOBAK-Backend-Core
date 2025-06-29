package com.sbpb.ddobak.server.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자 엔티티
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name; // 사용자가 입력한 이름

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE; // 계정 상태

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime lastLoginAt; // 마지막 로그인 시간

    @Builder
    public User(String appleId, String email, String name) {
        this.appleId = appleId;
        this.email = email;
        this.name = name;
        this.status = UserStatus.ACTIVE;
    }

    /**
     * 사용자 이름 수정
     */
    public void updateName(String name) {
        this.name = name;
    }

    /**
     * 마지막 로그인 시간 업데이트
     */
    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * 회원 탈퇴 처리
     */
    public void withdraw() {
        this.status = UserStatus.WITHDRAWN;
    }

    /**
     * 사용자 상태 열거형
     */
    public enum UserStatus {
        ACTIVE,     // 활성
        INACTIVE,   // 비활성
        WITHDRAWN   // 탈퇴
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
} 