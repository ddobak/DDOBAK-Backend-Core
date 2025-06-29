package com.sbpb.ddobak.server.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 공통 시스템 에러 코드 정의 (5xxx 범위)
 * 
 * 새로운 도메인별 코드 체계:
 * - 1xxx: Auth 도메인 (인증/인가)
 * - 2xxx: User 도메인 (사용자 관리)
 * - 3xxx: DocumentProcess 도메인 (문서 처리)
 * - 4xxx: ExternalContent 도메인 (외부 컨텐츠)
 * - 5xxx: Common/System (공통 시스템 에러)
 * 
 * 각 도메인 내 분류:
 * - x000~x099: 정상 또는 경고 수준
 * - x100~x999: 에러
 * 
 * Common/System 영역 세부 분류:
 * - 5000-5099: 정상/경고 (향후 확장용)
 * - 5100-5199: 일반적인 시스템 에러
 * - 5200-5299: 데이터베이스 에러
 * - 5300-5399: 외부 서비스 에러
 */
public interface ErrorCode {

    /**
     * HTTP 상태 코드 반환
     */
    HttpStatus getHttpStatus();

    /**
     * 에러 코드 반환
     */
    int getCode();

    /**
     * 에러 메시지 반환
     */
    String getMessage();

    /**
     * HTTP 상태 코드 값 반환
     */
    default int getStatusCode() {
        return getHttpStatus().value();
    }

    /**
     * 문자열 형태의 에러 코드 반환
     */
    default String getCodeAsString() {
        return String.valueOf(getCode());
    }
}