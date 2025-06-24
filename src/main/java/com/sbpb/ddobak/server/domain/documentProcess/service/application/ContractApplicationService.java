package com.sbpb.ddobak.server.domain.documentProcess.service.application;

import com.sbpb.ddobak.server.domain.documentProcess.dto.*;

/**
 * 계약서 처리 애플리케이션 서비스 (Application Layer)
 * Use Case를 조정하고 도메인 서비스들을 조합하여 전체 비즈니스 플로우를 관리
 */
public interface ContractApplicationService {

    /**
     * 분석 처리 요청
     */
    ContractAnalysisResponse processAnalysis(ContractAnalysisRequest request, String userId);
} 