package com.sbpb.ddobak.server.domain.documentProcess.dto;

/**
 * 계약서 분석 상태 enum
 * 향후 비동기 처리 및 푸시 알림용
 */
public enum AnalysisStatus {
    
    /**
     * 분석 요청됨 (Step Functions 실행 시작)
     */
    REQUESTED("분석 요청됨"),
    
    /**
     * 분석 진행 중 (Step Functions 실행 중)
     */
    IN_PROGRESS("분석 진행 중"),
    
    /**
     * OCR 처리 완료
     */
    OCR_COMPLETED("OCR 처리 완료"),
    
    /**
     * AI 분석 진행 중
     */
    AI_ANALYZING("AI 분석 진행 중"),
    
    /**
     * 분석 완료
     */
    COMPLETED("분석 완료"),
    
    /**
     * 분석 실패
     */
    FAILED("분석 실패"),
    
    /**
     * 분석 중단됨
     */
    CANCELLED("분석 중단됨");
    
    private final String description;
    
    AnalysisStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
} 