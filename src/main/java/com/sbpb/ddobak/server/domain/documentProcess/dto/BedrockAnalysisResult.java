package com.sbpb.ddobak.server.domain.documentProcess.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Bedrock AI 분석 결과 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BedrockAnalysisResult {

    /**
     * 원본 계약서 내용 (OCR 결과 종합)
     */
    private String originContent;
    
    /**
     * 계약서 요약
     */
    private String summary;
    
    /**
     * 또박이 코멘터리
     */
    private DdobakCommentary ddobakCommentary;
    
    /**
     * 독소 조항 목록
     */
    private List<ToxicClause> toxics;
} 