package com.sbpb.ddobak.server.domain.documentProcess.service.infrastructure;

import java.util.List;

/**
 * 외부 분석 서비스 인터페이스 (Infrastructure Layer)
 */
public interface ExternalAnalysisService {

    /**
     * 계약서 분석을 위한 Lambda 함수 호출
     * 
     * @param contractId 계약서 ID
     * @param ocrContents OCR 추출된 컨텐츠 목록
     * @return 분석 요청 ID 또는 상태
     */
    String requestContractAnalysis(String contractId, List<String> ocrContents);

    /**
     * 분석 결과 조회 (동기 방식)
     * 
     * @param contractId 계약서 ID
     * @param analysisRequestId 분석 요청 ID
     * @return 분석 결과 JSON 응답
     */
    String getAnalysisResult(String contractId, String analysisRequestId);
} 