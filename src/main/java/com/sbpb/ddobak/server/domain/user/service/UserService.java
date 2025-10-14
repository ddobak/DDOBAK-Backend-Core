package com.sbpb.ddobak.server.domain.user.service;

import com.sbpb.ddobak.server.common.exception.DuplicateResourceException;
import com.sbpb.ddobak.server.common.exception.ResourceNotFoundException;
import com.sbpb.ddobak.server.domain.auth.oauth.AppleOAuthClient;
import com.sbpb.ddobak.server.domain.auth.service.JwtService;
import com.sbpb.ddobak.server.domain.user.dto.CreateUserRequest;
import com.sbpb.ddobak.server.domain.user.dto.UserProfileRequest;
import com.sbpb.ddobak.server.domain.user.dto.UserProfileResponse;
import com.sbpb.ddobak.server.domain.user.dto.UserResponse;
import com.sbpb.ddobak.server.domain.user.dto.UserAnalysesResponse;
import com.sbpb.ddobak.server.domain.user.entity.User;
import com.sbpb.ddobak.server.domain.user.repository.UserRepository;
import com.sbpb.ddobak.server.domain.documentProcess.entity.ContractAnalysis;
import com.sbpb.ddobak.server.domain.documentProcess.repository.ContractAnalysisRepository;
import com.sbpb.ddobak.server.domain.documentProcess.repository.ToxicClauseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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
    private final ContractAnalysisRepository contractAnalysisRepository;
    private final ToxicClauseRepository toxicClauseRepository;
    private final AppleOAuthClient appleOAuthClient;

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
        log.info("Getting user profile: userId={}", userId);
        
        try {
            // 사용자 조회
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));
            
            // 사용자 활성 상태 확인
            if (Boolean.TRUE.equals(user.getIsDeleted())) {
                throw new IllegalArgumentException("비활성화된 사용자입니다: " + userId);
            }
            
            log.info("User profile retrieved successfully for userId: {}", userId);
            
            return UserProfileResponse.builder()
                .userId(userId)
                .email(user.getEmail())
                .name(user.getName())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
                
        } catch (Exception e) {
            log.error("Failed to get user profile: {}", e.getMessage());
            throw new IllegalArgumentException("사용자 프로필 조회 실패: " + e.getMessage());
        }
    }

    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserProfileRequest request) {
        log.info("Updating user profile: userId={}, name={}", userId, request.getName());

        try {
            // 사용자 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));

            // 사용자 활성 상태 확인
            if (Boolean.TRUE.equals(user.getIsDeleted())) {
                throw new IllegalArgumentException("비활성화된 사용자입니다: " + userId);
            }

            // 사용자 이름 업데이트
            user.updateProfile(request.getName());
            userRepository.save(user);

            log.info("User profile updated successfully for userId: {}", userId);

            return UserProfileResponse.builder()
                    .userId(userId)
                    .email(user.getEmail())
                    .name(user.getName())
                    .status(user.getStatus())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .lastLoginAt(user.getLastLoginAt())
                    .build();

        } catch (Exception e) {
            log.error("Failed to update user profile: {}", e.getMessage());
            throw new IllegalArgumentException("사용자 프로필 수정 실패: " + e.getMessage());
        }
    }

    @Transactional
    public void withdrawUser(Long userId) {
        log.info("Withdrawing user: userId={}", userId);
        
        try {
            // 사용자 조회
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));
            
            // 이미 탈퇴한 사용자인지 확인
            if (Boolean.TRUE.equals(user.getIsDeleted())) {
                throw new IllegalArgumentException("이미 탈퇴한 사용자입니다: " + userId);
            }
            
            // Apple 사용자인 경우 Apple 서버에서 계정 삭제
            if ("apple".equals(user.getOauthProvider()) && user.getAppleRefreshToken() != null) {
                try {
                    log.info("Revoking Apple token for user: {} ({})", user.getEmail(), userId);
                    appleOAuthClient.revokeToken(user.getAppleRefreshToken());
                    log.info("Apple token revoked successfully for user: {} ({})", user.getEmail(), userId);
                } catch (Exception e) {
                    // Apple Token Revocation 실패 시 경고 로그만 남기고 계속 진행
                    // (DB에서 삭제는 진행되어야 하므로)
                    log.warn("Failed to revoke Apple token for user: {} ({}). Error: {}", 
                        user.getEmail(), userId, e.getMessage());
                }
            } else if ("apple".equals(user.getOauthProvider())) {
                log.warn("Apple refresh token not found for user: {} ({}). Cannot revoke Apple token.", 
                    user.getEmail(), userId);
            }
            
            // 소프트 삭제 (isDeleted = true)
            user.delete();
            userRepository.save(user);
            
            log.info("User withdrawn successfully: userId={}", userId);
            
        } catch (Exception e) {
            log.error("Failed to withdraw user: {}", e.getMessage());
            throw new IllegalArgumentException("회원 탈퇴 실패: " + e.getMessage());
        }
    }
    
    /**
     * 사용자 분석 결과 조회
     */
    public UserAnalysesResponse getUserAnalyses(Long userId, int requestCount) {
        log.info("Getting user analyses: userId={}, requestCount={}", userId, requestCount);
        
        try {
            // 사용자 존재 확인
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + userId));
            
            // 사용자 활성 상태 확인
            if (Boolean.TRUE.equals(user.getIsDeleted())) {
                throw new IllegalArgumentException("비활성화된 사용자입니다: " + userId);
            }
            
            // 사용자 ID로 분석 결과들 조회
            List<ContractAnalysis> analyses = contractAnalysisRepository.findAnalysesByUserId(userId);
            
            // requestCount 제한 적용
            List<ContractAnalysis> limitedAnalyses = analyses.stream()
                .limit(requestCount)
                .collect(Collectors.toList());
            
            // ContractAnalysisDto로 변환
            List<UserAnalysesResponse.ContractAnalysisDto> contractDtos = limitedAnalyses.stream()
                .map(this::convertToContractAnalysisDto)
                .collect(Collectors.toList());
            
            log.info("User analyses retrieved successfully: userId={}, count={}", userId, contractDtos.size());
            
            return new UserAnalysesResponse(contractDtos.size(), contractDtos);
            
        } catch (Exception e) {
            log.error("Failed to get user analyses: {}", e.getMessage());
            throw new IllegalArgumentException("사용자 분석 결과 조회 실패: " + e.getMessage());
        }
    }
    
    /**
     * ContractAnalysis를 ContractAnalysisDto로 변환
     */
    private UserAnalysesResponse.ContractAnalysisDto convertToContractAnalysisDto(ContractAnalysis analysis) {
        // 독소 조항 개수 조회
        long toxicCount = toxicClauseRepository.countByAnalysisId(analysis.getId());
        
        // Contract에서 title과 contractType 가져오기 (Lambda에서 저장된 값)
        String contractTitle = analysis.getContract().getTitle() != null ? 
            analysis.getContract().getTitle() : "계약서 분석 중...";
        
        String contractType = analysis.getContract().getContractType() != null ? 
            analysis.getContract().getContractType().name().toLowerCase() : "general";
        
        // 분석 상태 (status 또는 processStatus 사용)
        String analysisStatus = analysis.getProcessStatus() != null ? 
            analysis.getProcessStatus().name().toLowerCase() : 
            (analysis.getStatus() != null ? analysis.getStatus() : "unknown");
        
        // 분석 날짜 포맷
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String analysisDate = analysis.getCreatedAt().format(formatter);
        
        return new UserAnalysesResponse.ContractAnalysisDto(
            analysis.getContractId(),
            contractTitle,
            contractType,
            analysisStatus,
            analysis.getId(),
            (int) toxicCount,
            analysisDate
        );
    }
} 