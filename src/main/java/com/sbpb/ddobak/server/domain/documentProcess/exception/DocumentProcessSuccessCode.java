package com.sbpb.ddobak.server.domain.documentProcess.exception;

import com.sbpb.ddobak.server.common.response.BaseSuccessCode;

/**
 * DocumentProcess 도메인 성공 코드 정의 (3xxx 범위)
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
 * DocumentProcess 도메인 성공 코드 체계:
 * - 3000-3099: 성공 응답
 */
public enum DocumentProcessSuccessCode implements BaseSuccessCode {

    // ===== 3000-3099: 성공 응답 =====
    OCR_SUCCESS(3000, "OCR processing completed successfully"),
    OCR_SUCCESS_EMPTY(3001, "OCR processing completed but no content found"),
    ANALYSIS_SUCCESS(3010, "Analysis processing completed successfully"),
    ANALYSIS_SUCCESS_EMPTY(3011, "Analysis completed but no significant information found"),
    ANALYSIS_RESULT_OK(3020, "Analysis result retrieved successfully"),
    SUMMARY_NOT_AVAILABLE(3021, "Analysis completed but summary not available");

    private final int code;
    private final String message;

    DocumentProcessSuccessCode(int code, String message) {
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