package com.sbpb.ddobak.server.domain.documentProcess.service.application.impl;

import com.sbpb.ddobak.server.domain.documentProcess.dto.*;
import com.sbpb.ddobak.server.domain.documentProcess.entity.Contract;
import com.sbpb.ddobak.server.domain.documentProcess.entity.ContractAnalysis;
import com.sbpb.ddobak.server.domain.documentProcess.exception.ContractExceptions;
import com.sbpb.ddobak.server.domain.documentProcess.repository.ContractAnalysisRepository;
import com.sbpb.ddobak.server.domain.documentProcess.repository.ContractRepository;
import com.sbpb.ddobak.server.domain.documentProcess.repository.OcrContentRepository;
import com.sbpb.ddobak.server.domain.documentProcess.service.application.DocumentProcessApplicationService;
import com.sbpb.ddobak.server.domain.documentProcess.service.domain.AnalysisService;
import com.sbpb.ddobak.server.domain.documentProcess.service.domain.OcrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * 문서 처리 Application Service 구현체
 * Clean Architecture Application Layer - Use Case 조합 및 전체 플로우 제어
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DocumentProcessApplicationServiceImpl implements DocumentProcessApplicationService {

    private final OcrService ocrService;
    private final AnalysisService analysisService;
    private final ContractRepository contractRepository;
    private final OcrContentRepository ocrContentRepository;
    private final ContractAnalysisRepository contractAnalysisRepository;

    @Override
    @Transactional
    public DocumentProcessResponse processDocumentComplete(String userId, List<MultipartFile> files, String contractType) {
        log.info("Starting complete document processing for user: {}, files: {}", userId, files.size());

        try {
            // 1. OCR 처리
            OcrResponse ocrResponse = ocrService.processMultipleFiles(userId, files, contractType);
            String contractId = ocrResponse.getContractId();

            log.info("OCR processing completed for contract: {}", contractId);

            // 2. 자동으로 분석 요청
            AnalysisResponse analysisResponse;
            try {
                analysisResponse = analysisService.requestAnalysis(contractId);
                log.info("Analysis requested automatically for contract: {}", contractId);
            } catch (Exception e) {
                log.warn("Failed to auto-request analysis for contract: {}: {}", contractId, e.getMessage());
                // 분석 요청 실패해도 OCR 결과는 반환
                analysisResponse = AnalysisResponse.builder()
                        .contractId(contractId)
                        .status("FAILED")
                        .message("Analysis request failed: " + e.getMessage())
                        .build();
            }

            // 3. 전체 응답 생성
            return DocumentProcessResponse.builder()
                    .contractId(contractId)
                    .ocrResults(ocrResponse.getOcrResults())
                    .analysisInfo(analysisResponse)
                    .processingStatus("OCR_COMPLETED_ANALYSIS_REQUESTED")
                    .message("Document processing completed successfully")
                    .build();

        } catch (Exception e) {
            log.error("Failed to process document complete for user: {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Complete document processing failed", e);
        }
    }

    @Override
    @Transactional
    public OcrResponse processOcrOnly(String userId, List<MultipartFile> files, String contractType) {
        log.info("Starting OCR-only processing for user: {}, files: {}", userId, files.size());

        try {
            return ocrService.processMultipleFiles(userId, files, contractType);
        } catch (Exception e) {
            log.error("Failed to process OCR only for user: {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("OCR processing failed", e);
        }
    }

    @Override
    @Transactional
    public void updateOcrContent(OcrContentUpdateRequest request) {
        log.info("Updating OCR content for contract: {}, page: {}", request.getContractId(), request.getPageIndex());

        try {
            ocrService.updateOcrContent(request.getContractId(), request.getPageIndex(), request.getContent());
            log.info("OCR content updated successfully for contract: {}, page: {}", 
                    request.getContractId(), request.getPageIndex());
        } catch (Exception e) {
            log.error("Failed to update OCR content for contract: {}, page: {}: {}", 
                    request.getContractId(), request.getPageIndex(), e.getMessage(), e);
            throw new RuntimeException("OCR content update failed", e);
        }
    }

    @Override
    @Transactional
    public AnalysisResponse requestAnalysis(AnalysisRequest request) {
        log.info("Requesting analysis for contract: {}", request.getContractId());

        try {
            return analysisService.requestAnalysis(request.getContractId());
        } catch (Exception e) {
            log.error("Failed to request analysis for contract: {}: {}", request.getContractId(), e.getMessage(), e);
            throw new RuntimeException("Analysis request failed", e);
        }
    }

    @Override
    public AnalysisResponse getAnalysisResult(String contractId) {
        log.info("Retrieving analysis result for contract: {}", contractId);

        try {
            return analysisService.getAnalysisResult(contractId);
        } catch (Exception e) {
            log.error("Failed to get analysis result for contract: {}: {}", contractId, e.getMessage(), e);
            throw new RuntimeException("Analysis result retrieval failed", e);
        }
    }

    @Override
    public List<OcrResponse.OcrResult> getOcrResults(String contractId) {
        log.info("Retrieving OCR results for contract: {}", contractId);

        try {
            return ocrService.getOcrResults(contractId);
        } catch (Exception e) {
            log.error("Failed to get OCR results for contract: {}: {}", contractId, e.getMessage(), e);
            throw new RuntimeException("OCR results retrieval failed", e);
        }
    }

    @Override
    public DocumentProcessStatusResponse getProcessStatus(String contractId) {
        log.info("Retrieving process status for contract: {}", contractId);

        try {
            // 1. 계약서 정보 조회
            Contract contract = contractRepository.findById(contractId)
                    .orElseThrow(() -> ContractExceptions.contractNotFound(contractId));

            // 2. OCR 상태 확인
            Long ocrCount = ocrContentRepository.countByContractId(contractId);
            String ocrStatus = ocrCount > 0 ? "COMPLETED" : "NOT_STARTED";

            // 3. 분석 상태 확인
            String analysisStatus = analysisService.getAnalysisStatus(contractId);

            // 4. 전체 상태 결정
            String overallStatus = determineOverallStatus(ocrStatus, analysisStatus);

            // 5. 분석 정보 조회 (존재하는 경우)
            Optional<ContractAnalysis> analysis = contractAnalysisRepository.findByContractId(contractId);

            return DocumentProcessStatusResponse.builder()
                    .contractId(contractId)
                    .ocrStatus(ocrStatus)
                    .analysisStatus(analysisStatus)
                    .overallStatus(overallStatus)
                    .totalPages(ocrCount.intValue())
                    .createdAt(contract.getCreatedAt())
                    .updatedAt(analysis.map(ContractAnalysis::getUpdatedAt).orElse(contract.getUpdatedAt()))
                    .statusMessage(generateStatusMessage(overallStatus))
                    .build();

        } catch (Exception e) {
            log.error("Failed to get process status for contract: {}: {}", contractId, e.getMessage(), e);
            throw new RuntimeException("Process status retrieval failed", e);
        }
    }

    /**
     * 전체 상태 결정
     */
    private String determineOverallStatus(String ocrStatus, String analysisStatus) {
        if ("NOT_STARTED".equals(ocrStatus)) {
            return "NOT_STARTED";
        } else if ("COMPLETED".equals(ocrStatus) && "NOT_STARTED".equals(analysisStatus)) {
            return "OCR_COMPLETED";
        } else if ("COMPLETED".equals(ocrStatus) && "IN_PROGRESS".equals(analysisStatus)) {
            return "ANALYSIS_IN_PROGRESS";
        } else if ("COMPLETED".equals(ocrStatus) && "COMPLETED".equals(analysisStatus)) {
            return "ALL_COMPLETED";
        } else {
            return "IN_PROGRESS";
        }
    }

    /**
     * 상태별 메시지 생성
     */
    private String generateStatusMessage(String overallStatus) {
        return switch (overallStatus) {
            case "NOT_STARTED" -> "Document processing has not started";
            case "OCR_COMPLETED" -> "OCR processing completed, analysis not started";
            case "ANALYSIS_IN_PROGRESS" -> "OCR completed, analysis in progress";
            case "ALL_COMPLETED" -> "All processing completed successfully";
            case "IN_PROGRESS" -> "Document processing in progress";
            default -> "Unknown processing status";
        };
    }
} 