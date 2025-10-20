package com.sbpb.ddobak.server.domain.documentProcess.service;

import com.sbpb.ddobak.server.domain.documentProcess.dto.analysis.AnalysisRequest;
import com.sbpb.ddobak.server.domain.documentProcess.dto.analysis.AnalysisResponse;
import com.sbpb.ddobak.server.domain.documentProcess.dto.analysis.AnalysisResultResponse;
import com.sbpb.ddobak.server.domain.documentProcess.dto.ocr.OcrContentResponse;
import com.sbpb.ddobak.server.domain.documentProcess.dto.ocr.OcrRequest;
import com.sbpb.ddobak.server.domain.documentProcess.dto.ocr.OcrResponse;
import com.sbpb.ddobak.server.domain.documentProcess.dto.ocr.OcrUpdateRequest;

/**
 * 문서 처리 서비스의 최상위 인터페이스
 * OCR 처리와 계약서 분석 기능을 제공
 */
public interface DocumentProcessService {

    /**
     * OCR 처리 요청
     */
    OcrResponse processOcr(Long userId, OcrRequest request);

    /**
     * OCR 결과 조회
     * @param contractId 계약서 ID
     * @param userId 사용자 ID (소유자 검증용)
     */
    OcrContentResponse getOcrResults(String contractId, Long userId);

    /**
     * OCR 내용 수정
     * @param contractId 계약서 ID
     * @param request 수정 요청 데이터
     * @param userId 사용자 ID (소유자 검증용)
     */
    void updateOcrContent(String contractId, OcrUpdateRequest request, Long userId);

    /**
     * 분석 요청
     * @param request 분석 요청 데이터
     * @param userId 사용자 ID (소유자 검증용)
     */
    AnalysisResponse requestAnalysis(AnalysisRequest request, Long userId);

    /**
     * 분석 결과 조회
     * @param contractId 계약서 ID
     * @param analysisId 분석 ID
     * @param userId 사용자 ID (소유자 검증용)
     */
    AnalysisResultResponse getAnalysisResult(String contractId, String analysisId, Long userId);

    /**
     * 계약서 삭제
     * @param contractId 계약서 ID
     * @param userId 사용자 ID (소유자 검증용)
     */
    void deleteContract(String contractId, Long userId);
} 