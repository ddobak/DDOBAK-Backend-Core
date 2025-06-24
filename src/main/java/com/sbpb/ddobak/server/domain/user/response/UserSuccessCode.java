package com.sbpb.ddobak.server.domain.user.response;

import com.sbpb.ddobak.server.common.response.SuccessCode;

/**
 * User 도메인 성공 코드 정의 (2xxx 범위)
 * 
 * User 도메인 성공 코드 체계:
 * - 2000-2099: 정상 또는 경고 수준
 * 
 * User 도메인 성공 코드 세부 분류:
 * - 2000-2009: 사용자 프로필 관련 성공
 * - 2010-2019: 사용자 인증 관련 성공
 * - 2020-2029: 사용자 상태 관련 성공
 */
public enum UserSuccessCode implements SuccessCode {

    // ===== 2000-2009: 사용자 프로필 관련 성공 =====
    PROFILE_CREATED(2000, "사용자 프로필이 성공적으로 생성되었습니다"),
    PROFILE_RETRIEVED(2001, "사용자 프로필을 성공적으로 조회했습니다"),
    PROFILE_UPDATED(2002, "사용자 프로필이 성공적으로 수정되었습니다"),
    PROFILE_DELETED(2003, "사용자 프로필이 성공적으로 삭제되었습니다"),

    // ===== 2010-2019: 사용자 인증 관련 성공 =====
    USER_LOGIN_SUCCESS(2010, "로그인이 성공했습니다"),
    USER_LOGOUT_SUCCESS(2011, "로그아웃이 성공했습니다"),
    USER_REGISTERED(2012, "사용자 등록이 성공했습니다"),

    // ===== 2020-2029: 사용자 상태 관련 성공 =====
    USER_ACTIVATED(2020, "사용자 계정이 활성화되었습니다"),
    USER_DEACTIVATED(2021, "사용자 계정이 비활성화되었습니다"),
    USER_WITHDRAWN(2022, "회원 탈퇴가 성공적으로 처리되었습니다");

    private final int code;
    private final String message;

    UserSuccessCode(int code, String message) {
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