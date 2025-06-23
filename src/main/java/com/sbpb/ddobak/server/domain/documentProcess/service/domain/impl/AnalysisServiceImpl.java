package com.sbpb.ddobak.server.domain.documentProcess.service.domain.impl;

import com.sbpb.ddobak.server.common.utils.IdGenerator;
import com.sbpb.ddobak.server.domain.documentProcess.dto.AnalysisResponse;
import com.sbpb.ddobak.server.domain.documentProcess.entity.Contract;
import com.sbpb.ddobak.server.domain.documentProcess.entity.ContractAnalysis;
import com.sbpb.ddobak.server.domain.documentProcess.entity.OcrContent;
import com.sbpb.ddobak.server.domain.documentProcess.exception.ContractExceptions;
import com.sbpb.ddobak.server.domain.documentProcess.repository.ContractAnalysisRepository;
import com.sbpb.ddobak.server.domain.documentProcess.repository.ContractRepository;
import com.sbpb.ddobak.server.domain.documentProcess.repository.OcrContentRepository;
import com.sbpb.ddobak.server.domain.documentProcess.service.domain.AnalysisService;
import com.sbpb.ddobak.server.domain.documentProcess.service.infrastructure.ExternalAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 분석 도메인 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AnalysisServiceImpl implements AnalysisService {

    private final ContractRepository contractRepository;
    private final OcrContentRepository ocrContentRepository;
    private final ContractAnalysisRepository contractAnalysisRepository;
    private final ExternalAnalysisService externalAnalysisService;

    @Override
    @Transactional
    public AnalysisResponse requestAnalysis(String contractId) {
        log.info("Requesting analysis for contract: {}", contractId);

        // 1. 계약서 존재 여부 확인
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> ContractExceptions.contractNotFound(contractId));

        // 2. OCR 컨텐츠 조회
        List<OcrContent> ocrContents = ocrContentRepository.findByContractIdOrderByTagIdx(contractId);
        if (ocrContents.isEmpty()) {
            throw new IllegalStateException("No OCR content found for contract: " + contractId);
        }

        // 3. 이미 분석이 진행 중이거나 완료된 경우 확인
        Optional<ContractAnalysis> existingAnalysis = contractAnalysisRepository.findByContractId(contractId);
        if (existingAnalysis.isPresent()) {
            log.warn("Analysis already exists for contract: {}", contractId);
            return AnalysisResponse.builder()
                    .contractId(contractId)
                    .analysisId(existingAnalysis.get().getId())
                    .status("ALREADY_EXISTS")
                    .message("Analysis already exists for this contract")
                    .build();
        }

        // 4. 분석 엔티티 생성 (초기 상태)
        ContractAnalysis analysis = createInitialAnalysis(contractId);

        // 5. OCR 컨텐츠를 문자열 리스트로 변환
        List<String> ocrContentStrings = ocrContents.stream()
                .map(OcrContent::getContent)
                .toList();

        // 6. 외부 분석 서비스 호출
        try {
            String analysisRequestId = externalAnalysisService.requestContractAnalysis(contractId, ocrContentStrings);
            
            log.info("Analysis requested successfully: contractId={}, analysisId={}, requestId={}", 
                    contractId, analysis.getId(), analysisRequestId);

            return AnalysisResponse.builder()
                    .contractId(contractId)
                    .analysisId(analysis.getId())
                    .status("REQUESTED")
                    .message("Analysis request submitted successfully")
                    .build();

        } catch (Exception e) {
            log.error("Failed to request analysis for contract: {}: {}", contractId, e.getMessage(), e);
            
            // 실패한 경우 분석 엔티티 삭제
            contractAnalysisRepository.delete(analysis);
            
            throw new RuntimeException("Failed to request analysis", e);
        }
    }

    @Override
    public AnalysisResponse getAnalysisResult(String contractId) {
        log.info("Retrieving analysis result for contract: {}", contractId);

        // 1. 계약서 존재 여부 확인
        if (!contractRepository.existsById(contractId)) {
            throw ContractExceptions.contractNotFound(contractId);
        }

        // 2. 분석 결과 조회
        ContractAnalysis analysis = contractAnalysisRepository.findByContractId(contractId)
                .orElseThrow(() -> ContractExceptions.analysisResultNotFound(contractId));

        // 3. 분석 상태 확인
        String status = determineAnalysisStatus(analysis);

        return AnalysisResponse.builder()
                .contractId(contractId)
                .analysisId(analysis.getId())
                .status(status)
                .message(generateStatusMessage(status))
                .build();
    }

    @Override
    public String getAnalysisStatus(String contractId) {
        log.info("Checking analysis status for contract: {}", contractId);

        // 1. 계약서 존재 여부 확인
        if (!contractRepository.existsById(contractId)) {
            throw ContractExceptions.contractNotFound(contractId);
        }

        // 2. 분석 상태 확인
        Optional<ContractAnalysis> analysis = contractAnalysisRepository.findByContractId(contractId);
        
        if (analysis.isEmpty()) {
            return "NOT_STARTED";
        }

        return determineAnalysisStatus(analysis.get());
    }

    /**
     * 초기 분석 엔티티 생성
     */
    private ContractAnalysis createInitialAnalysis(String contractId) {
        ContractAnalysis analysis = ContractAnalysis.builder()
                .id(IdGenerator.generate())
                .contractId(contractId)
                .summary("Analysis in progress...")
                .ddobakOverallComment("")
                .ddobakWarningComment("")
                .ddobakAdvice("")
                .build();

        return contractAnalysisRepository.save(analysis);
    }

    /**
     * 분석 상태 결정
     */
    private String determineAnalysisStatus(ContractAnalysis analysis) {
        // 간단한 상태 결정 로직
        // 실제로는 더 복잡한 상태 관리가 필요할 수 있음
        if (analysis.getSummary() != null && !analysis.getSummary().equals("Analysis in progress...")) {
            return "COMPLETED";
        } else {
            return "IN_PROGRESS";
        }
    }

    /**
     * 상태별 메시지 생성
     */
    private String generateStatusMessage(String status) {
        return switch (status) {
            case "COMPLETED" -> "Analysis completed successfully";
            case "IN_PROGRESS" -> "Analysis is in progress";
            case "NOT_STARTED" -> "Analysis has not been started";
            default -> "Unknown analysis status";
        };
    }
} 