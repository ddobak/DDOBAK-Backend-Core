package com.sbpb.ddobak.server.domain.documentProcess.exception;

import com.sbpb.ddobak.server.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * DocumentProcess 도메인 전용 예외 처리기
 */
@RestControllerAdvice(basePackages = "com.sbpb.ddobak.server.domain.documentProcess")
@Order(1) // 글로벌 핸들러보다 우선
@Slf4j
public class DocumentProcessExceptionHandler {

    /**
     * DocumentProcess 도메인 비즈니스 예외 처리
     */
    @ExceptionHandler(DocumentProcessBusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDocumentProcessBusinessException(
            DocumentProcessBusinessException e) {
        
        log.warn("DocumentProcess business exception occurred: {}", e.getLoggingMessage());

        return ResponseEntity
            .status(e.getErrorCode().getHttpStatus())
            .body(ApiResponse.error(e.getErrorCode().getCode(), e.getMessage()));
    }
} 