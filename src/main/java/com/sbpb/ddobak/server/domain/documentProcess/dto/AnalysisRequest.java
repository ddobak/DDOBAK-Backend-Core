package com.sbpb.ddobak.server.domain.documentProcess.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.AllArgsConstructor;

/**
 * 분석 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisRequest {

    /**
     * 분석할 계약서 ID
     */
    @NotBlank(message = "Contract ID is required")
    private String contractId;
} 