package com.sbpb.ddobak.server.domain.documentProcess.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 전체 문서 처리 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentProcessResponse {

    /**
     * 계약서 ID
     */
    private String contractId;

    /**
     * OCR 결과 목록
     */
    private List<OcrResponse.OcrResult> ocrResults;

    /**
     * 분석 요청 정보
     */
    private AnalysisResponse analysisInfo;

    /**
     * 전체 처리 상태
     */
    private String processingStatus;

    /**
     * 처리 메시지
     */
    private String message;
} 