package com.sbpb.ddobak.server.domain.documentProcess.service;

import com.sbpb.ddobak.server.domain.documentProcess.dto.analysis.AnalysisRequest;
import com.sbpb.ddobak.server.domain.documentProcess.dto.analysis.AnalysisResponse;
import com.sbpb.ddobak.server.domain.documentProcess.dto.analysis.AnalysisResultResponse;
import com.sbpb.ddobak.server.domain.documentProcess.dto.ocr.OcrContentResponse;
import com.sbpb.ddobak.server.domain.documentProcess.dto.ocr.OcrRequest;
import com.sbpb.ddobak.server.domain.documentProcess.dto.ocr.OcrResponse;
import com.sbpb.ddobak.server.domain.documentProcess.dto.ocr.OcrUpdateRequest;
import org.springframework.stereotype.Service;

/**
 * 문서 처리 서비스 Facade 구현체
 * OCR과 분석 서비스를 조합하여 클라이언트에 단일 진입점 제공
 */
@Service
public class DocumentProcessServiceImpl implements DocumentProcessService {

    private final OcrProcessService ocrProcessService;
    private final AnalysisProcessService analysisProcessService;

    public DocumentProcessServiceImpl(OcrProcessService ocrProcessService,
                                      AnalysisProcessService analysisProcessService) {
        this.ocrProcessService = ocrProcessService;
        this.analysisProcessService = analysisProcessService;
    }

    @Override
    public OcrResponse processOcr(Long userId, OcrRequest request) {
        return ocrProcessService.processOcr(userId, request);
    }

    @Override
    public OcrContentResponse getOcrResults(String contractId, Long userId) {
        // 계약서 소유자 검증
        verifyContractOwner(contractId, userId);
        return ocrProcessService.getOcrResults(contractId);
    }

    @Override
    public void updateOcrContent(String contractId, OcrUpdateRequest request, Long userId) {
        // 계약서 소유자 검증
        verifyContractOwner(contractId, userId);
        ocrProcessService.updateOcrContent(contractId, request);
    }

    @Override
    public AnalysisResponse requestAnalysis(AnalysisRequest request, Long userId) {
        // 계약서 소유자 검증
        verifyContractOwner(request.getContractId(), userId);
        return analysisProcessService.requestAnalysis(request);
    }

    @Override
    public AnalysisResultResponse getAnalysisResult(String contractId, String analysisId, Long userId) {
        // 계약서 소유자 검증
        verifyContractOwner(contractId, userId);
        return analysisProcessService.getAnalysisResult(contractId, analysisId);
    }
    
    /**
     * 계약서 소유자 검증
     * @param contractId 계약서 ID
     * @param userId 사용자 ID
     * @throws com.sbpb.ddobak.server.common.exception.ResourceNotFoundException 계약서가 존재하지 않는 경우
     * @throws com.sbpb.ddobak.server.common.exception.BusinessException 권한이 없는 경우
     */
    private void verifyContractOwner(String contractId, Long userId) {
        // 계약서 소유자 검증 로직은 OcrProcessService에 위임
        ocrProcessService.verifyContractOwner(contractId, userId);
    }
} 