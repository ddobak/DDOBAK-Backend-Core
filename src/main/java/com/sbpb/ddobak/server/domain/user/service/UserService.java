package com.sbpb.ddobak.server.domain.user.service;

import com.sbpb.ddobak.server.domain.user.dto.UserProfileRequest;
import com.sbpb.ddobak.server.domain.user.dto.UserProfileResponse;
import com.sbpb.ddobak.server.domain.user.entity.User;
import com.sbpb.ddobak.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

/**
 * 사용자 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /**
     * 사용자 프로필 저장 (새 사용자 생성)
     * @param request 사용자 프로필 요청
     * @return 사용자 ID 응답
     */
    @Transactional
    public UserProfileResponse.UserIdResponse saveUserProfile(UserProfileRequest request) {
        log.info("사용자 프로필 저장 요청: name={}", request.getName());

        // 임시 사용자 생성 (실제로는 애플 로그인 후 생성)
        User user = User.builder()
                .appleId("temp_apple_id_" + System.currentTimeMillis())
                .email("temp@example.com")
                .name(request.getName())
                .build();

        User savedUser = userRepository.save(user);
        log.info("사용자 프로필 저장 완료: userId={}", savedUser.getId());

        return UserProfileResponse.UserIdResponse.builder()
                .userId(savedUser.getId())
                .build();
    }

    /**
     * 사용자 프로필 조회
     * @param userId 사용자 ID
     * @return 사용자 프로필 정보
     */
    public UserProfileResponse getUserProfile(Long userId) {
        log.info("사용자 프로필 조회 요청: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없음: userId={}", userId);
                    return new IllegalArgumentException("사용자를 찾을 수 없습니다.");
                });

        return UserProfileResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt().format(DATE_FORMATTER))
                .lastLoginAt(user.getLastLoginAt() != null ? 
                        user.getLastLoginAt().format(DATE_FORMATTER) : null)
                .build();
    }

    /**
     * 사용자 프로필 수정
     * @param userId 사용자 ID
     * @param request 수정할 프로필 정보
     * @return 수정된 사용자 프로필 정보
     */
    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserProfileRequest request) {
        log.info("사용자 프로필 수정 요청: userId={}, name={}", userId, request.getName());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없음: userId={}", userId);
                    return new IllegalArgumentException("사용자를 찾을 수 없습니다.");
                });

        // 사용자 상태 확인
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            log.error("비활성 사용자 프로필 수정 시도: userId={}, status={}", userId, user.getStatus());
            throw new IllegalArgumentException("비활성 계정입니다.");
        }

        // 이름 수정
        user.updateName(request.getName());
        User savedUser = userRepository.save(user);
        log.info("사용자 프로필 수정 완료: userId={}", savedUser.getId());

        return UserProfileResponse.builder()
                .userId(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .status(savedUser.getStatus().name())
                .createdAt(savedUser.getCreatedAt().format(DATE_FORMATTER))
                .lastLoginAt(savedUser.getLastLoginAt() != null ? 
                        savedUser.getLastLoginAt().format(DATE_FORMATTER) : null)
                .build();
    }

    /**
     * 회원 탈퇴
     * @param userId 사용자 ID
     */
    @Transactional
    public void withdrawUser(Long userId) {
        log.info("회원 탈퇴 요청: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없음: userId={}", userId);
                    return new IllegalArgumentException("사용자를 찾을 수 없습니다.");
                });

        // 이미 탈퇴한 사용자인지 확인
        if (user.getStatus() == User.UserStatus.WITHDRAWN) {
            log.error("이미 탈퇴한 사용자: userId={}", userId);
            throw new IllegalArgumentException("이미 탈퇴 처리된 계정입니다.");
        }

        // 회원 탈퇴 처리
        user.withdraw();
        userRepository.save(user);
        log.info("회원 탈퇴 완료: userId={}", userId);
    }

    /**
     * 사용자 존재 여부 확인
     * @param userId 사용자 ID
     * @return 존재 여부
     */
    public boolean existsUser(Long userId) {
        return userRepository.existsById(userId);
    }

    /**
     * 활성 사용자 여부 확인
     * @param userId 사용자 ID
     * @return 활성 여부
     */
    public boolean isActiveUser(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getStatus() == User.UserStatus.ACTIVE)
                .orElse(false);
    }
} 