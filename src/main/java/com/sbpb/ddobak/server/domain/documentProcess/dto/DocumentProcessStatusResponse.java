package com.sbpb.ddobak.server.domain.documentProcess.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 문서 처리 상태 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentProcessStatusResponse {

    /**
     * 계약서 ID
     */
    private String contractId;

    /**
     * OCR 처리 상태
     */
    private String ocrStatus;

    /**
     * 분석 처리 상태
     */
    private String analysisStatus;

    /**
     * 전체 처리 상태
     */
    private String overallStatus;

    /**
     * OCR 페이지 수
     */
    private Integer totalPages;

    /**
     * 생성 시간
     */
    private LocalDateTime createdAt;

    /**
     * 마지막 업데이트 시간
     */
    private LocalDateTime updatedAt;

    /**
     * 상태 메시지
     */
    private String statusMessage;
} 