package com.sbpb.ddobak.server.common.exception;

import com.sbpb.ddobak.server.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.http.converter.HttpMessageNotReadableException;

/**
 * 전역 예외 처리기
 * 
 * 모든 예외를 중앙에서 처리하여 일관된 에러 응답 제공
 * 
 * 처리하는 예외 유형:
 * - 비즈니스 예외 (BusinessException)
 * - 검증 예외 (Validation)
 * - 시스템 예외 (RuntimeException, Exception)
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 비즈니스 예외 처리
     * 애플리케이션에서 의도적으로 발생시킨 예외들을 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("Business exception occurred: {}", e.getLoggingMessage());

        return ResponseEntity
            .status(e.getErrorCode().getHttpStatus())
            .body(ApiResponse.error(e.getErrorCode(), e.getMessage()));
    }

    /**
     * MethodArgumentNotValidException 처리
     * Bean Validation 실패 시 발생
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        return ResponseEntity
            .status(CommonErrorCode.INVALID_INPUT.getHttpStatus())
            .body(ApiResponse.error(CommonErrorCode.INVALID_INPUT, message));
    }

    /**
     * MaxUploadSizeExceededException 처리
     * 파일 업로드 크기 초과 시 발생
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        return ResponseEntity
            .status(CommonErrorCode.INVALID_INPUT.getHttpStatus())
            .body(ApiResponse.error(CommonErrorCode.INVALID_INPUT, "File size exceeds the allowed limit"));
    }

    /**
     * HttpMessageNotReadableException 처리
     * JSON 파싱 실패 시 발생
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return ResponseEntity
            .status(CommonErrorCode.INVALID_INPUT.getHttpStatus())
            .body(ApiResponse.error(CommonErrorCode.INVALID_INPUT, e.getMessage()));
    }

    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Invalid argument: {}", e.getMessage());

        return ResponseEntity
            .status(CommonErrorCode.INVALID_INPUT.getHttpStatus())
            .body(ApiResponse.error(CommonErrorCode.INVALID_INPUT, e.getMessage()));
    }

    /**
     * 일반적인 예외 처리
     * 처리되지 않은 모든 예외를 캐치
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        log.error("Unhandled exception occurred", e);
        return ResponseEntity
            .status(CommonErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
            .body(ApiResponse.error(CommonErrorCode.INTERNAL_SERVER_ERROR, "An unexpected error occurred"));
    }
}