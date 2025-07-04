package com.sbpb.ddobak.server.domain.user.service;

import com.sbpb.ddobak.server.common.exception.DuplicateResourceException;
import com.sbpb.ddobak.server.common.exception.ResourceNotFoundException;
import com.sbpb.ddobak.server.common.utils.IdGenerator;
import com.sbpb.ddobak.server.domain.user.dto.CreateUserRequest;
import com.sbpb.ddobak.server.domain.user.dto.UserProfileRequest;
import com.sbpb.ddobak.server.domain.user.dto.UserProfileResponse;
import com.sbpb.ddobak.server.domain.user.dto.UserResponse;
import com.sbpb.ddobak.server.domain.user.entity.User;
import com.sbpb.ddobak.server.domain.user.exception.UserErrorCode;
import com.sbpb.ddobak.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * 사용자 생성 (테스트용)
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.getEmail());

        // 이메일 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw DuplicateResourceException.email(request.getEmail());
        }

        // 사용자 생성
        User user = User.builder()
            .email(request.getEmail())
            .name(request.getName())
            .nickname(request.getNickname())
            .status(User.UserStatus.ACTIVE)
            .build();

        User savedUser = userRepository.save(user);

        log.info("User created successfully with ID: {}", savedUser.getId());

        return UserResponse.from(savedUser);
    }

    /**
     * 사용자 프로필 저장
     */
    @Transactional
    public UserProfileResponse.UserIdResponse saveUserProfile(UserProfileRequest request) {
        log.info("Saving user profile with name: {}", request.getName());

        // 이름 유효성 검사
        validateUserName(request.getName());

        // 이메일을 이름으로 사용하여 임시 사용자 생성 (실제로는 인증된 사용자 정보를 사용해야 함)
        String tempEmail = request.getName() + "@temp.com";
        
        User user = User.builder()
            .email(tempEmail)
            .name(request.getName())
            .status(User.UserStatus.ACTIVE)
            .build();

        User savedUser = userRepository.save(user);

        log.info("User profile saved successfully with ID: {}", savedUser.getId());

        return UserProfileResponse.UserIdResponse.builder()
            .userId(savedUser.getId())
            .build();
    }

    /**
     * 사용자 프로필 조회
     */
    public UserProfileResponse getUserProfile(Long userId) {
        log.info("Getting user profile for userId: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // 삭제된 사용자인지 확인
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new IllegalArgumentException("탈퇴한 사용자입니다");
        }

        // 비활성 사용자인지 확인
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new IllegalArgumentException("비활성 상태의 사용자입니다");
        }

        return UserProfileResponse.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .status(user.getStatus())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .lastLoginAt(user.getLastLoginAt())
            .build();
    }

    /**
     * 사용자 프로필 수정
     */
    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserProfileRequest request) {
        log.info("Updating user profile for userId: {}, name: {}", userId, request.getName());

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // 삭제된 사용자인지 확인
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new IllegalArgumentException("탈퇴한 사용자입니다");
        }

        // 비활성 사용자인지 확인
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new IllegalArgumentException("비활성 상태의 사용자입니다");
        }

        // 이름 유효성 검사
        validateUserName(request.getName());

        // 프로필 업데이트
        user.updateProfile(request.getName(), null, null);
        User updatedUser = userRepository.save(user);

        log.info("User profile updated successfully for userId: {}", userId);

        return UserProfileResponse.builder()
            .userId(updatedUser.getId())
            .email(updatedUser.getEmail())
            .name(updatedUser.getName())
            .status(updatedUser.getStatus())
            .createdAt(updatedUser.getCreatedAt())
            .updatedAt(updatedUser.getUpdatedAt())
            .lastLoginAt(updatedUser.getLastLoginAt())
            .build();
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public void withdrawUser(Long userId) {
        log.info("Withdrawing user with userId: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        // 이미 탈퇴한 사용자인지 확인
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new IllegalArgumentException("이미 탈퇴한 사용자입니다");
        }

        // 비활성 사용자인지 확인
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new IllegalArgumentException("비활성 상태의 사용자입니다");
        }

        // 소프트 삭제 처리
        user.delete();
        userRepository.save(user);

        log.info("User withdrawn successfully with userId: {}", userId);
    }

    /**
     * 사용자 이름 유효성 검사
     */
    private void validateUserName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("이름은 필수입니다");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("이름은 50자 이하여야 합니다");
        }
    }
} 