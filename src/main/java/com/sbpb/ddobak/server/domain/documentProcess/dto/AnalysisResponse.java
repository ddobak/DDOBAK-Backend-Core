package com.sbpb.ddobak.server.domain.documentProcess.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 분석 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResponse {

    /**
     * 계약서 ID
     */
    private String contractId;

    /**
     * 분석 ID
     */
    private String analysisId;

    /**
     * 분석 상태
     */
    private String status;

    /**
     * 분석 요청 메시지
     */
    private String message;
} 