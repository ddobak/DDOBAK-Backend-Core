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
    public OcrResponse processOcr(String userId, OcrRequest request) {
        return ocrProcessService.processOcr(userId, request);
    }

    @Override
    public OcrContentResponse getOcrResults(String contractId) {
        return ocrProcessService.getOcrResults(contractId);
    }

    @Override
    public void updateOcrContent(String contractId, OcrUpdateRequest request) {
        ocrProcessService.updateOcrContent(contractId, request);
    }

    @Override
    public AnalysisResultResponse requestAnalysis(AnalysisRequest request) {
        return analysisProcessService.requestAnalysis(request);
    }

    @Override
    public AnalysisResultResponse getAnalysisResult(String contractId, String analysisId) {
        return analysisProcessService.getAnalysisResult(contractId, analysisId);
    }
} 