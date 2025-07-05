package com.sbpb.ddobak.server.domain.user.service;

import com.sbpb.ddobak.server.common.exception.DuplicateResourceException;
import com.sbpb.ddobak.server.common.exception.ResourceNotFoundException;
import com.sbpb.ddobak.server.domain.auth.service.JwtService;
import com.sbpb.ddobak.server.domain.user.dto.CreateUserRequest;
import com.sbpb.ddobak.server.domain.user.dto.UserProfileRequest;
import com.sbpb.ddobak.server.domain.user.dto.UserProfileResponse;
import com.sbpb.ddobak.server.domain.user.dto.UserResponse;
import com.sbpb.ddobak.server.domain.user.entity.User;
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
    private final JwtService jwtService;

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
            .build();

        User savedUser = userRepository.save(user);

        log.info("User created successfully with ID: {}", savedUser.getId());

        return UserResponse.from(savedUser);
    }

    /**
     * 사용자 프로필 저장 (기존 사용자의 name 업데이트)
     * JWT 토큰에서 사용자 ID를 추출하여 기존 사용자의 프로필을 업데이트
     */
    @Transactional
    public UserProfileResponse.UserIdResponse saveUserProfile(UserProfileRequest request, String accessToken) {
        log.info("Updating user profile with name: {}", request.getName());
        
        try {
            // JWT 토큰 유효성 검증
            if (!jwtService.isTokenValid(accessToken)) {
                throw new IllegalArgumentException("유효하지 않은 JWT 토큰입니다.");
            }
            
            // JWT 토큰에서 사용자 ID 추출
            Long userId = jwtService.getUserIdFromToken(accessToken);
            log.info("Updating user profile for userId: {}, name: {}", userId, request.getName());
            
            // 기존 사용자 조회
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));
            
            // 사용자 이름 업데이트
            user.updateProfile(request.getName());
            userRepository.save(user);
            
            log.info("User profile updated successfully for userId: {}", userId);
            
            return UserProfileResponse.UserIdResponse.builder()
                .userId(userId)
                .build();
                
        } catch (Exception e) {
            log.error("Failed to update user profile: {}", e.getMessage());
            throw new IllegalArgumentException("사용자 프로필 업데이트 실패: " + e.getMessage());
        }
    }

    // UserController에서 필요한 메서드들 - 최소 구현
    @Transactional
    public UserProfileResponse.UserIdResponse saveUserProfile(UserProfileRequest request) {
        // 이 메서드는 JWT 토큰 없이 호출되므로 사용하지 않음
        throw new UnsupportedOperationException("JWT 토큰이 필요합니다. saveUserProfile(request, accessToken) 메서드를 사용하세요.");
    }

    public UserProfileResponse getUserProfile(Long userId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public UserProfileResponse updateUserProfile(Long userId, UserProfileRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void withdrawUser(Long userId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
} 