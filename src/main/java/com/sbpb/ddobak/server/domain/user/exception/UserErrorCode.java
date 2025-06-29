package com.sbpb.ddobak.server.domain.user.exception;

import org.springframework.http.HttpStatus;

import com.sbpb.ddobak.server.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * User 도메인 에러 코드 정의 (2xxx 범위)
 * 
 * User 도메인 에러 코드 체계:
 * - 2000-2099: 정상 또는 경고 수준 (향후 확장용)
 * - 2100-2999: 에러
 * 
 * User 도메인 에러 코드 세부 분류:
 * - 2100-2199: 사용자 프로필 관련 에러
 * - 2200-2299: 사용자 인증 관련 에러
 * - 2300-2399: 사용자 상태 관련 에러
 * - 2400-2499: 사용자 데이터 검증 에러
 */
@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    // ===== 2100-2199: 사용자 프로필 관련 에러 =====
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 2100, "사용자를 찾을 수 없습니다"),
    USER_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, 2101, "사용자 프로필을 찾을 수 없습니다"),
    USER_PROFILE_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 2102, "사용자 프로필 생성에 실패했습니다"),
    USER_PROFILE_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 2103, "사용자 프로필 수정에 실패했습니다"),
    USER_PROFILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 2104, "사용자 프로필 삭제에 실패했습니다"),

    // ===== 2200-2299: 사용자 인증 관련 에러 =====
    USER_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, 2200, "사용자 인증에 실패했습니다"),
    USER_AUTHORIZATION_FAILED(HttpStatus.FORBIDDEN, 2201, "사용자 권한이 없습니다"),
    USER_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, 2202, "로그인에 실패했습니다"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, 2203, "이미 존재하는 사용자입니다"),
    USER_APPLE_ID_NOT_FOUND(HttpStatus.NOT_FOUND, 2204, "애플 ID를 찾을 수 없습니다"),

    // ===== 2300-2399: 사용자 상태 관련 에러 =====
    USER_INACTIVE(HttpStatus.FORBIDDEN, 2300, "비활성 상태의 사용자입니다"),
    USER_WITHDRAWN(HttpStatus.FORBIDDEN, 2301, "탈퇴한 사용자입니다"),
    USER_ALREADY_WITHDRAWN(HttpStatus.CONFLICT, 2302, "이미 탈퇴 처리된 사용자입니다"),
    USER_ACTIVATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 2303, "사용자 계정 활성화에 실패했습니다"),
    USER_DEACTIVATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 2304, "사용자 계정 비활성화에 실패했습니다"),

    // ===== 2400-2499: 사용자 데이터 검증 에러 =====
    INVALID_USER_NAME(HttpStatus.BAD_REQUEST, 2400, "유효하지 않은 사용자 이름입니다"),
    INVALID_USER_EMAIL(HttpStatus.BAD_REQUEST, 2401, "유효하지 않은 이메일 형식입니다"),
    INVALID_USER_APPLE_ID(HttpStatus.BAD_REQUEST, 2402, "유효하지 않은 애플 ID입니다"),
    USER_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, 2403, "사용자 이름이 너무 깁니다"),
    USER_NAME_TOO_SHORT(HttpStatus.BAD_REQUEST, 2404, "사용자 이름이 너무 짧습니다"),
    USER_NAME_REQUIRED(HttpStatus.BAD_REQUEST, 2405, "사용자 이름은 필수입니다");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    /**
     * HTTP 상태 코드 반환
     */
    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    /**
     * 문자열 형태의 에러 코드 반환
     */
    public String getCodeAsString() {
        return String.valueOf(code);
    }
} 