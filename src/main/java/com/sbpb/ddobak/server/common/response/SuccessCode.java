package com.sbpb.ddobak.server.common.response;

/**
 * 공통 시스템 성공 코드 정의 (5xxx 범위)
 * 
 * 새로운 도메인별 코드 체계:
 * - 1xxx: Auth 도메인 (인증/인가)
 * - 2xxx: User 도메인 (사용자 관리)
 * - 3xxx: DocumentProcess 도메인 (문서 처리)
 * - 4xxx: ExternalContent 도메인 (외부 컨텐츠)
 * - 5xxx: Common/System (공통 시스템)
 * 
 * 각 도메인 내 분류:
 * - x000~x099: 정상 또는 경고 수준
 * - x100~x999: 에러
 * 
 * Common/System 성공 코드 체계:
 * - 5000-5099: 일반적인 성공 응답
 */
public interface SuccessCode {

    /**
     * 성공 코드 반환
     */
    int getCode();

    /**
     * 성공 메시지 반환
     */
    String getMessage();

    /**
     * 문자열 형태의 성공 코드 반환
     */
    String getCodeAsString();
}