package com.sbpb.ddobak.server.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    // ===== 5100-5199: 일반적인 시스템 에러 =====
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 5100, "Internal server error occurred"),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, 5101, "Service temporarily unavailable"),
    CONFIGURATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 5102, "Configuration error"),
    INITIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 5103, "Initialization error"),
    PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 5104, "Processing error occurred"),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, 5110, "Invalid input provided"),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, 5111, "Invalid type value"),
    MISSING_INPUT_VALUE(HttpStatus.BAD_REQUEST, 5112, "Required input value is missing"),
    INVALID_JSON_FORMAT(HttpStatus.BAD_REQUEST, 5113, "Invalid JSON format"),
    INVALID_PARAMETER_FORMAT(HttpStatus.BAD_REQUEST, 5114, "Invalid parameter format"),
    INVALID_HEADER_VALUE(HttpStatus.BAD_REQUEST, 5115, "Invalid header value"),
    INVALID_QUERY_PARAMETER(HttpStatus.BAD_REQUEST, 5116, "Invalid query parameter"),
    REQUEST_BODY_MISSING(HttpStatus.BAD_REQUEST, 5117, "Request body is missing"),
    INVALID_CONTENT_TYPE(HttpStatus.BAD_REQUEST, 5118, "Invalid content type"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, 5119, "HTTP method not allowed"),
    
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, 5150, "Resource not found"),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, 5151, "Entity not found"),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, 5160, "Resource already exists"),
    DUPLICATE_KEY_VALUE(HttpStatus.CONFLICT, 5161, "Duplicate key value"),
    RESOURCE_ALREADY_EXISTS(HttpStatus.CONFLICT, 5162, "Resource already exists"),
    VERSION_CONFLICT(HttpStatus.CONFLICT, 5170, "Version conflict detected"),
    OPTIMISTIC_LOCK_FAILURE(HttpStatus.CONFLICT, 5171, "Optimistic lock failure"),

    // ===== 5200-5299: 데이터베이스 에러 =====
    DATABASE_CONNECTION_ERROR(HttpStatus.SERVICE_UNAVAILABLE, 5200, "Database connection error"),
    DATABASE_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, 5201, "Database operation timeout"),
    TRANSACTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 5202, "Transaction failed"),
    CONSTRAINT_VIOLATION(HttpStatus.CONFLICT, 5203, "Database constraint violation"),
    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, 5204, "Data integrity violation"),
    SQL_SYNTAX_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 5205, "SQL syntax error"),
    DATABASE_LOCK_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, 5206, "Database lock timeout"),
    
    // ===== 5300-5399: 외부 서비스 에러 =====
    EXTERNAL_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, 5300, "External service unavailable"),
    EXTERNAL_SERVICE_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, 5301, "External service timeout"),
    EXTERNAL_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, 5302, "External service error"),
    EXTERNAL_API_RATE_LIMIT(HttpStatus.TOO_MANY_REQUESTS, 5303, "External API rate limit exceeded"),
    EXTERNAL_SERVICE_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, 5304, "External service authentication failed"),
    EXTERNAL_SERVICE_AUTHORIZATION_FAILED(HttpStatus.FORBIDDEN, 5305, "External service authorization failed"),
    CIRCUIT_BREAKER_OPEN(HttpStatus.SERVICE_UNAVAILABLE, 5350, "Circuit breaker is open");

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
} 