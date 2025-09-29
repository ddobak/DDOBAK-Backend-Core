package com.sbpb.ddobak.server.domain.documentProcess.service;

import com.sbpb.ddobak.server.common.exception.ResourceNotFoundException;
import com.sbpb.ddobak.server.common.utils.IdGenerator;
import com.sbpb.ddobak.server.common.utils.LambdaUtil;
import com.sbpb.ddobak.server.config.AwsConfig;
import com.sbpb.ddobak.server.domain.documentProcess.dto.analysis.AnalysisRequest;
import com.sbpb.ddobak.server.domain.documentProcess.dto.analysis.AnalysisResponse;
import com.sbpb.ddobak.server.domain.documentProcess.dto.analysis.AnalysisResultResponse;
import com.sbpb.ddobak.server.domain.documentProcess.dto.lambda.AnalysisLambdaPayload;
import com.sbpb.ddobak.server.domain.documentProcess.entity.Contract;
import com.sbpb.ddobak.server.domain.documentProcess.entity.ContractAnalysis;
import com.sbpb.ddobak.server.domain.documentProcess.entity.OcrContent;
import com.sbpb.ddobak.server.domain.documentProcess.entity.ToxicClause;
import com.sbpb.ddobak.server.domain.documentProcess.exception.ContractAccessDeniedException;
import com.sbpb.ddobak.server.domain.documentProcess.repository.ContractAnalysisRepository;
import com.sbpb.ddobak.server.domain.documentProcess.repository.ContractRepository;
import com.sbpb.ddobak.server.domain.documentProcess.repository.OcrContentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 계약서 분석 처리 전담 서비스
 */
@Service
@Transactional
public class AnalysisProcessService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisProcessService.class);

    private final OcrContentRepository ocrContentRepository;
    private final ContractAnalysisRepository contractAnalysisRepository;
    private final ContractRepository contractRepository;
    private final LambdaUtil lambdaUtil;
    private final AwsConfig awsConfig;

    public AnalysisProcessService(OcrContentRepository ocrContentRepository,
                                  ContractAnalysisRepository contractAnalysisRepository,
                                  ContractRepository contractRepository,
                                  LambdaUtil lambdaUtil,
                                  AwsConfig awsConfig) {
        this.ocrContentRepository = ocrContentRepository;
        this.contractAnalysisRepository = contractAnalysisRepository;
        this.contractRepository = contractRepository;
        this.lambdaUtil = lambdaUtil;
        this.awsConfig = awsConfig;
    }

    /**
     * 분석 요청
     */
    public AnalysisResponse requestAnalysis(AnalysisRequest request) {
        // 1. 기존 분석 존재 여부 확인
        ContractAnalysis existingAnalysis = contractAnalysisRepository.findByContractId(request.getContractId());
        if (existingAnalysis != null) {
            return new AnalysisResponse(existingAnalysis.getId());
        }
        
        // 2. 분석 레코드 생성
        String analysisId = IdGenerator.generate();
        ContractAnalysis analysis = new ContractAnalysis(
            analysisId, 
            request.getContractId(), 
            null, 
            "in_progress"
        );
        contractAnalysisRepository.save(analysis);

        try {
            // 3. OCR 결과 조회
            List<OcrContent> ocrContents = ocrContentRepository.findByContractIdOrderByTagIdx(request.getContractId());
            
            // 4. Analysis Lambda 비동기 호출
            List<String> contractTexts = ocrContents.stream()
                .map(OcrContent::getContent)
                .collect(Collectors.toList());
            
            AnalysisLambdaPayload payload = new AnalysisLambdaPayload(request.getContractId(), analysisId, contractTexts);
            
            log.debug("Analysis Lambda 호출 준비 - ContractId: {}, AnalysisId: {}, OCR 페이지 수: {}", 
                     request.getContractId(), analysisId, contractTexts.size());
            
            lambdaUtil.invokeAsync(awsConfig.getLambda().getAnalysisFunctionName(), payload)
                .thenAccept(response -> {
                    log.debug("Analysis Lambda 호출 성공 - ContractId: {}, StatusCode: {}", 
                             request.getContractId(), response.statusCode());
                });
            
            return new AnalysisResponse(analysisId);
            
        } catch (Exception e) {
            log.error("분석 처리 실패 - ContractId: {}, AnalysisId: {}", request.getContractId(), analysisId, e);
            analysis.setStatus("failed");
            contractAnalysisRepository.save(analysis);
            throw new RuntimeException("Failed to request analysis", e);
        }
    }

    /**
     * 분석 결과 조회
     */
    @Transactional(readOnly = true)
    public AnalysisResultResponse getAnalysisResult(String contractId, String analysisId) {
        // 분석 결과 조회
        ContractAnalysis analysis = contractAnalysisRepository.findByIdWithToxicClauses(analysisId)
            .orElseThrow(() -> new ResourceNotFoundException("Analysis", "id", analysisId));
        
        if (!analysis.getContractId().equals(contractId)) {
            throw new ResourceNotFoundException("Analysis for contract", "contractId", contractId);
        }
        
        // OCR 원본 내용 조회
        List<OcrContent> ocrContents = ocrContentRepository.findByContractIdOrderByTagIdx(contractId);
        StringBuilder originContent = new StringBuilder();
        for (OcrContent content : ocrContents) {
            originContent.append(content.getContent());
        }
        
        // 독소 조항 변환
        List<AnalysisResultResponse.ToxicClauseDto> toxics = new ArrayList<>();
        for (ToxicClause toxicClause : analysis.getToxicClauses()) {
            toxics.add(new AnalysisResultResponse.ToxicClauseDto(
                toxicClause.getTitle(),
                toxicClause.getClause(),
                toxicClause.getReason(),
                toxicClause.getReasonReference(),
                toxicClause.getWarnLevel()
            ));
        }
        
        // 응답 생성
        AnalysisResultResponse response = new AnalysisResultResponse();
        response.setOriginContent(originContent.toString());
        response.setSummary(analysis.getSummary());
        response.setAnalysisStatus(analysis.getStatus());
        response.setAnalysisDate(analysis.getCreatedAt());
        response.setToxicCount(analysis.getToxicClauses().size());
        response.setDdobakCommentary(new AnalysisResultResponse.DdobakCommentary(
            analysis.getDdobakOverallComment(),
            analysis.getDdobakWarningComment(),
            analysis.getDdobakAdvice()
        ));
        response.setToxics(toxics);
        
        return response;
    }
    
    /**
     * 계약서 소유자 검증
     * 계약서 소유자가 아닌 경우 예외 발생
     * 
     * @param contractId 계약서 ID
     * @param userId 사용자 ID
     * @throws ResourceNotFoundException 계약서를 찾을 수 없는 경우
     * @throws ContractAccessDeniedException 계약서 소유자가 아닌 경우
     */
    @Transactional(readOnly = true)
    public void verifyContractOwner(String contractId, Long userId) {
        log.debug("계약서 소유자 검증 - ContractId: {}, UserId: {}", contractId, userId);
        
        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", contractId));
        
        if (!contract.getUserId().equals(userId)) {
            log.warn("계약서 접근 권한 없음 - ContractId: {}, 요청 UserId: {}, 소유자 UserId: {}", 
                    contractId, userId, contract.getUserId());
            throw new ContractAccessDeniedException(contractId, userId);
        }
        
        log.debug("계약서 소유자 검증 성공 - ContractId: {}, UserId: {}", contractId, userId);
    }
} 