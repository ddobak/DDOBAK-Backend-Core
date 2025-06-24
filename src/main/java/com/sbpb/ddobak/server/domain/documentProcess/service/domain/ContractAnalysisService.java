package com.sbpb.ddobak.server.domain.documentProcess.service.domain;

import com.sbpb.ddobak.server.domain.documentProcess.dto.ContractAnalysisRequest;
import com.sbpb.ddobak.server.domain.documentProcess.dto.ContractAnalysisResponse;
import com.sbpb.ddobak.server.domain.documentProcess.dto.AnalysisStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 계약서 분석 도메인 서비스 (Domain Layer)
 * 분석 관련 핵심 비즈니스 로직을 담당
 */
public interface ContractAnalysisService {
    
    /**
     * 분석 처리 수행 (즉시 응답)
     */
    ContractAnalysisResponse processAnalysis(ContractAnalysisRequest request, String userId);
    
    /**
     * 분석용 파일 목록 검증
     */
    void validateAnalysisFiles(List<MultipartFile> files);
    
    // ===== 향후 비동기 확장용 메서드들 =====
    
    /**
     * Step Functions 실행 상태 조회
     * 향후 비동기 처리 시 사용
     */
    AnalysisStatus getAnalysisStatus(String contractId);
    
    /**
     * Step Functions 완료 결과 파싱
     * 향후 푸시 알림 처리 시 사용
     */
    ContractAnalysisResponse parseStepFunctionsResult(Map<String, Object> stepFunctionsOutput);
} 