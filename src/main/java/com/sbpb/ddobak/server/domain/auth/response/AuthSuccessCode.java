package com.sbpb.ddobak.server.domain.auth.response;

import com.sbpb.ddobak.server.common.response.BaseSuccessCode;

/**
 * Auth 도메인 성공 코드 정의 (1xxx 범위)
 * 
 * Auth 도메인 성공 코드 체계:
 * - 1000-1099: 정상 또는 경고 수준
 * 
 * Auth 도메인 성공 코드 세부 분류:
 * - 1000-1009: 로그인 관련 성공
 * - 1010-1019: 토큰 관련 성공
 * - 1020-1029: 로그아웃 관련 성공
 */
public enum AuthSuccessCode implements BaseSuccessCode {

    // ===== 1000-1009: 로그인 관련 성공 =====
    LOGIN_SUCCESS(1000, "로그인이 성공했습니다"),
    APPLE_LOGIN_SUCCESS(1001, "Apple 로그인이 성공했습니다"),
    GOOGLE_LOGIN_SUCCESS(1002, "Google 로그인이 성공했습니다"),
    KAKAO_LOGIN_SUCCESS(1003, "카카오 로그인이 성공했습니다"),

    // ===== 1010-1019: 토큰 관련 성공 =====
    TOKEN_REFRESHED(1010, "토큰이 성공적으로 갱신되었습니다"),
    TOKEN_VALIDATED(1011, "토큰이 유효합니다"),
    TOKEN_EXPIRED_WARNING(1012, "토큰이 곧 만료됩니다"),

    // ===== 1020-1029: 로그아웃 관련 성공 =====
    LOGOUT_SUCCESS(1020, "로그아웃이 성공했습니다");

    private final int code;
    private final String message;

    AuthSuccessCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 성공 코드 반환
     */
    @Override
    public int getCode() {
        return code;
    }

    /**
     * 성공 메시지 반환
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * 문자열 형태의 성공 코드 반환
     */
    @Override
    public String getCodeAsString() {
        return String.valueOf(code);
    }
} 