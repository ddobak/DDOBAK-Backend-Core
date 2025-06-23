package com.sbpb.ddobak.server.domain.documentProcess.service.application;

import com.sbpb.ddobak.server.domain.documentProcess.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 문서 처리 Application Service
 * Clean Architecture의 Application Layer - Use Case 조합 및 플로우 제어
 */
public interface DocumentProcessApplicationService {

    /**
     * 전체 문서 처리 플로우: OCR 처리 + 자동 분석 요청
     * 
     * @param userId 사용자 ID
     * @param files 업로드된 파일 목록
     * @param contractType 계약서 유형 (선택사항)
     * @return OCR 결과 및 분석 요청 정보
     */
    DocumentProcessResponse processDocumentComplete(String userId, List<MultipartFile> files, String contractType);

    /**
     * OCR만 처리 (분석 요청 없음)
     * 
     * @param userId 사용자 ID
     * @param files 업로드된 파일 목록
     * @param contractType 계약서 유형 (선택사항)
     * @return OCR 결과
     */
    OcrResponse processOcrOnly(String userId, List<MultipartFile> files, String contractType);

    /**
     * OCR 컨텐츠 수정
     * 
     * @param request OCR 컨텐츠 수정 요청
     */
    void updateOcrContent(OcrContentUpdateRequest request);

    /**
     * 분석 요청 (OCR 완료 후 호출)
     * 
     * @param request 분석 요청
     * @return 분석 요청 결과
     */
    AnalysisResponse requestAnalysis(AnalysisRequest request);

    /**
     * 분석 결과 조회
     * 
     * @param contractId 계약서 ID
     * @return 분석 결과
     */
    AnalysisResponse getAnalysisResult(String contractId);

    /**
     * 계약서의 OCR 결과 조회
     * 
     * @param contractId 계약서 ID
     * @return OCR 결과 목록
     */
    List<OcrResponse.OcrResult> getOcrResults(String contractId);

    /**
     * 전체 문서 처리 상태 조회
     * 
     * @param contractId 계약서 ID
     * @return 처리 상태 정보
     */
    DocumentProcessStatusResponse getProcessStatus(String contractId);
} 