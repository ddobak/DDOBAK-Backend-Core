package com.sbpb.ddobak.server.domain.documentProcess.exception;

import org.springframework.http.HttpStatus;

import com.sbpb.ddobak.server.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * DocumentProcess 도메인의 에러 코드 정의 (3xxx 범위)
 */
@Getter
@RequiredArgsConstructor
public enum DocumentProcessErrorCode implements ErrorCode {

    // ===== 3100-3199: 계약서 관련 에러 =====
    CONTRACT_NOT_FOUND(HttpStatus.NOT_FOUND, 3100, "계약서를 찾을 수 없습니다"),
    CONTRACT_OWNER_MISMATCH(HttpStatus.FORBIDDEN, 3101, "계약서에 접근 권한이 없습니다"),
    CONTRACT_TYPE_INVALID(HttpStatus.BAD_REQUEST, 3102, "유효하지 않은 계약서 유형입니다"),
    
    // ===== 3200-3299: OCR 관련 에러 =====
    OCR_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, 3200, "OCR 내용을 찾을 수 없습니다"),
    OCR_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 3201, "OCR 처리 중 오류가 발생했습니다"),
    OCR_UPDATE_FAILED(HttpStatus.BAD_REQUEST, 3202, "OCR 내용 업데이트에 실패했습니다"),
    
    // ===== 3300-3399: 분석 관련 에러 =====
    ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND, 3300, "분석 결과를 찾을 수 없습니다"),
    ANALYSIS_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 3301, "분석 처리 중 오류가 발생했습니다"),
    ANALYSIS_IN_PROGRESS(HttpStatus.ACCEPTED, 3302, "분석이 진행 중입니다"),
    ANALYSIS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 3303, "분석에 실패했습니다");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
