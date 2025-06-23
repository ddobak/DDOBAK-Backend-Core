package com.sbpb.ddobak.server.domain.documentProcess.service.domain;

import com.sbpb.ddobak.server.domain.documentProcess.dto.AnalysisResponse;

/**
 * 계약서 분석 도메인 서비스
 */
public interface AnalysisService {

    /**
     * 계약서 분석 요청
     * 
     * @param contractId 계약서 ID
     * @return 분석 요청 결과
     */
    AnalysisResponse requestAnalysis(String contractId);

    /**
     * 분석 결과 조회
     * 
     * @param contractId 계약서 ID
     * @return 분석 결과 (분석 완료된 경우)
     */
    AnalysisResponse getAnalysisResult(String contractId);

    /**
     * 분석 상태 확인
     * 
     * @param contractId 계약서 ID
     * @return 분석 상태 정보
     */
    String getAnalysisStatus(String contractId);
} 