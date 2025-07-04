package com.sbpb.ddobak.server.domain.user.service;

import com.sbpb.ddobak.server.common.exception.DuplicateResourceException;
import com.sbpb.ddobak.server.common.utils.IdGenerator;
import com.sbpb.ddobak.server.domain.user.dto.CreateUserRequest;
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
            .build();

        User savedUser = userRepository.save(user);

        log.info("User created successfully with ID: {}", savedUser.getId());

        return UserResponse.from(savedUser);
    }
} 