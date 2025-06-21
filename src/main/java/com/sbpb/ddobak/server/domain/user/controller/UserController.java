package com.sbpb.ddobak.server.domain.user.controller;

import com.sbpb.ddobak.server.common.response.ApiResponse;
import com.sbpb.ddobak.server.common.response.ResponseCode;
import com.sbpb.ddobak.server.domain.user.dto.UserProfileRequest;
import com.sbpb.ddobak.server.domain.user.dto.UserProfileResponse;
import com.sbpb.ddobak.server.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 API 컨트롤러
 * 사용자 프로필 관리 API를 제공.
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 기본 정보 입력
     * POST /user/profile
     * 
     * @param request 사용자 프로필 요청
     * @return 사용자 ID 응답
     */
    @PostMapping("/profile")
    public ApiResponse<UserProfileResponse.UserIdResponse> saveUserProfile(
            @Valid @RequestBody UserProfileRequest request) {
        log.info("사용자 프로필 저장 요청: name={}", request.getName());
        
        try {
            UserProfileResponse.UserIdResponse response = userService.saveUserProfile(request);
            return ApiResponse.success(response, ResponseCode.UserCode.PROFILE_SAVED);
        } catch (IllegalArgumentException e) {
            log.error("사용자 프로필 저장 실패: {}", e.getMessage());
            return ApiResponse.error(ResponseCode.UserCode.INVALID_NAME, e.getMessage());
        } catch (Exception e) {
            log.error("사용자 프로필 저장 중 오류 발생: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCode.SystemCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 정보 조회
     * GET /user/profile
     * 
     * @param userId 사용자 ID (쿼리 파라미터)
     * @return 사용자 프로필 정보
     */
    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> getUserProfile(@RequestParam Long userId) {
        log.info("사용자 프로필 조회 요청: userId={}", userId);
        
        try {
            UserProfileResponse response = userService.getUserProfile(userId);
            return ApiResponse.success(response, ResponseCode.UserCode.PROFILE_RETRIEVED);
        } catch (IllegalArgumentException e) {
            log.error("사용자 프로필 조회 실패: {}", e.getMessage());
            return ApiResponse.error(ResponseCode.UserCode.USER_NOT_FOUND);
        } catch (Exception e) {
            log.error("사용자 프로필 조회 중 오류 발생: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCode.SystemCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 개인정보 수정
     * PUT /user/profile
     * 
     * @param userId 사용자 ID (쿼리 파라미터)
     * @param request 수정할 프로필 정보
     * @return 수정된 사용자 프로필 정보
     */
    @PutMapping("/profile")
    public ApiResponse<UserProfileResponse> updateUserProfile(
            @RequestParam Long userId,
            @Valid @RequestBody UserProfileRequest request) {
        log.info("사용자 프로필 수정 요청: userId={}, name={}", userId, request.getName());
        
        try {
            UserProfileResponse response = userService.updateUserProfile(userId, request);
            return ApiResponse.success(response, ResponseCode.UserCode.PROFILE_UPDATED);
        } catch (IllegalArgumentException e) {
            log.error("사용자 프로필 수정 실패: {}", e.getMessage());
            if (e.getMessage().contains("찾을 수 없습니다")) {
                return ApiResponse.error(ResponseCode.UserCode.USER_NOT_FOUND);
            } else if (e.getMessage().contains("비활성")) {
                return ApiResponse.error(ResponseCode.UserCode.INVALID_USER_STATUS);
            } else {
                return ApiResponse.error(ResponseCode.UserCode.INVALID_NAME);
            }
        } catch (Exception e) {
            log.error("사용자 프로필 수정 중 오류 발생: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCode.SystemCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 회원 탈퇴
     * DELETE /user/withdraw
     * 
     * @param userId 사용자 ID (쿼리 파라미터)
     * @return 탈퇴 완료 응답
     */
    @DeleteMapping("/withdraw")
    public ApiResponse<Void> withdrawUser(@RequestParam Long userId) {
        log.info("회원 탈퇴 요청: userId={}", userId);
        
        try {
            userService.withdrawUser(userId);
            return ApiResponse.success(ResponseCode.UserCode.USER_WITHDRAWN);
        } catch (IllegalArgumentException e) {
            log.error("회원 탈퇴 실패: {}", e.getMessage());
            if (e.getMessage().contains("찾을 수 없습니다")) {
                return ApiResponse.error(ResponseCode.UserCode.USER_NOT_FOUND);
            } else if (e.getMessage().contains("이미 탈퇴")) {
                return ApiResponse.error(ResponseCode.UserCode.USER_ALREADY_WITHDRAWN);
            } else {
                return ApiResponse.error(ResponseCode.UserCode.INVALID_USER_STATUS);
            }
        } catch (Exception e) {
            log.error("회원 탈퇴 중 오류 발생: {}", e.getMessage(), e);
            return ApiResponse.error(ResponseCode.SystemCode.INTERNAL_SERVER_ERROR);
        }
    }
} 