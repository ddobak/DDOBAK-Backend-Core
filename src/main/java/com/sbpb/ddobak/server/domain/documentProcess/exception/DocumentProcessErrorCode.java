package com.sbpb.ddobak.server.domain.documentProcess.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * DocumentProcess 도메인 에러 코드 정의 (3xxx 범위)
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
 * DocumentProcess 도메인 세부 분류:
 * - 3100-3199: 분석 요청 관련 에러
 * - 3200-3299: 분석 처리 관련 에러
 * - 3300-3399: 결과 조회 관련 에러
 * - 3400-3499: 기타 문서 처리 에러
 */
@Getter
@RequiredArgsConstructor
public enum DocumentProcessErrorCode {

    // ===== 3100-3199: 분석 요청 관련 에러 =====
    ANALYSIS_INVALID_REQUEST(HttpStatus.BAD_REQUEST, 3100, "Invalid analysis request format"),
    ANALYSIS_FILE_MISSING(HttpStatus.BAD_REQUEST, 3101, "Image file is missing"),
    ANALYSIS_UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST, 3102, "Unsupported file type"),
    ANALYSIS_FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, 3103, "File size exceeds limit"),
    ANALYSIS_IMAGE_DECODING_FAILED(HttpStatus.BAD_REQUEST, 3104, "Image decoding failed"),
    ANALYSIS_IMAGE_EMPTY(HttpStatus.BAD_REQUEST, 3105, "No recognizable content in image"),
    ANALYSIS_CONTRACT_TYPE_INVALID(HttpStatus.BAD_REQUEST, 3106, "Invalid contract type"),
    ANALYSIS_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 3150, "Analysis processing failed"),
    ANALYSIS_ENGINE_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, 3151, "Analysis processing timeout"),
    ANALYSIS_ENGINE_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 3152, "Analysis engine internal error"),

    // ===== 3200-3299: 분석 처리 관련 에러 =====
    CONTRACT_ID_MISSING(HttpStatus.BAD_REQUEST, 3200, "Contract ID is missing"),
    CONTRACT_NOT_FOUND(HttpStatus.NOT_FOUND, 3201, "Contract not found"),
    ANALYSIS_ALREADY_EXISTS(HttpStatus.CONFLICT, 3202, "Analysis already exists for this contract"),
    ANALYSIS_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, 3203, "Analysis result not found"),
    IMAGE_NOT_MARKED(HttpStatus.BAD_REQUEST, 3204, "Image marking is required"),
    STEP_FUNCTIONS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 3250, "Step Functions execution failed"),
    ANALYSIS_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, 3251, "Analysis timeout"),
    TEXT_EXTRACTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 3252, "Text extraction failed"),
    ANALYSIS_SERVER_DOWN(HttpStatus.SERVICE_UNAVAILABLE, 3253, "Analysis server unavailable"),
    ANALYSIS_RESULT_PARSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 3254, "Analysis result parsing failed"),
    DATABASE_WRITE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 3255, "Database write failed"),

    // ===== 3300-3399: 결과 조회 관련 에러 =====
    TOXIC_CLAUSE_NOT_FOUND(HttpStatus.OK, 3300, "Analysis completed but no toxic clauses found"),
    ANALYSIS_ID_NOT_FOUND(HttpStatus.NOT_FOUND, 3301, "Analysis ID not found"),
    CONTRACT_ID_NOT_FOUND(HttpStatus.NOT_FOUND, 3302, "Contract ID not found"),
    UNAUTHORIZED_CONTRACT_ACCESS(HttpStatus.FORBIDDEN, 3303, "Unauthorized contract access"),
    RESULT_LOADING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 3350, "Result loading failed"),
    RESULT_FORMATTING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 3351, "Result formatting failed"),
    UNKNOWN_WEBVIEW_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 3352, "Unknown webview error");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    /**
     * HTTP 상태 코드 반환
     */
    public int getStatusCode() {
        return httpStatus.value();
    }

    /**
     * 문자열 형태의 에러 코드 반환
     */
    public String getCodeAsString() {
        return String.valueOf(code);
    }
} 