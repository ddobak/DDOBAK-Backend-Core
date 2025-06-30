package com.sbpb.ddobak.server.domain.documentProcess.service;

import com.sbpb.ddobak.server.common.utils.IdGenerator;
import com.sbpb.ddobak.server.common.utils.LambdaUtil;
import com.sbpb.ddobak.server.config.AwsConfig;
import com.sbpb.ddobak.server.domain.documentProcess.dto.analysis.AnalysisRequest;
import com.sbpb.ddobak.server.domain.documentProcess.dto.analysis.AnalysisResponse;
import com.sbpb.ddobak.server.domain.documentProcess.dto.analysis.AnalysisResultResponse;
import com.sbpb.ddobak.server.domain.documentProcess.dto.lambda.AnalysisLambdaPayload;
import com.sbpb.ddobak.server.domain.documentProcess.dto.lambda.AnalysisLambdaResponse;
import com.sbpb.ddobak.server.domain.documentProcess.entity.ContractAnalysis;
import com.sbpb.ddobak.server.domain.documentProcess.entity.OcrContent;
import com.sbpb.ddobak.server.domain.documentProcess.entity.ToxicClause;
import com.sbpb.ddobak.server.domain.documentProcess.repository.ContractAnalysisRepository;
import com.sbpb.ddobak.server.domain.documentProcess.repository.OcrContentRepository;
import com.sbpb.ddobak.server.domain.documentProcess.repository.ToxicClauseRepository;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
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
    private final ToxicClauseRepository toxicClauseRepository;
    private final LambdaUtil lambdaUtil;
    private final AwsConfig awsConfig;

    public AnalysisProcessService(OcrContentRepository ocrContentRepository,
                                  ContractAnalysisRepository contractAnalysisRepository,
                                  ToxicClauseRepository toxicClauseRepository,
                                  LambdaUtil lambdaUtil,
                                  AwsConfig awsConfig) {
        this.ocrContentRepository = ocrContentRepository;
        this.contractAnalysisRepository = contractAnalysisRepository;
        this.toxicClauseRepository = toxicClauseRepository;
        this.lambdaUtil = lambdaUtil;
        this.awsConfig = awsConfig;
    }

    /**
     * 분석 요청 및 결과 반환
     */
    public AnalysisResultResponse requestAnalysis(AnalysisRequest request) {
        // 1. 분석 레코드 생성
        String analysisId = IdGenerator.generate();
        ContractAnalysis analysis = new ContractAnalysis(
            analysisId, 
            request.getContractId(), 
            null, 
            "in_progress"
        );
        contractAnalysisRepository.save(analysis);

        try {
            // 2. OCR 결과 조회
            List<OcrContent> ocrContents = ocrContentRepository.findByContractIdOrderByTagIdx(request.getContractId());
            
            // 3. Analysis Lambda 동기 호출
            List<String> contractTexts = ocrContents.stream()
                .map(OcrContent::getContent)
                .collect(Collectors.toList());
            
            AnalysisLambdaPayload payload = new AnalysisLambdaPayload(contractTexts);
            
            log.debug("Analysis Lambda 호출 준비 - ContractId: {}, AnalysisId: {}, OCR 페이지 수: {}", 
                     request.getContractId(), analysisId, contractTexts.size());
            
            InvokeResponse response = lambdaUtil.invokeAndWait(
                awsConfig.getLambda().getAnalysisFunctionName(), 
                payload
            );
            
            log.debug("Analysis Lambda 호출 완료 - ContractId: {}, StatusCode: {}", 
                     request.getContractId(), response.statusCode());
            
            // 4. Lambda 응답 처리
            if (lambdaUtil.hasError(response)) {
                log.error("Analysis Lambda 오류 - ContractId: {}, Response: {}", 
                         request.getContractId(), response.payload().asUtf8String());
                analysis.setStatus("failed");
                contractAnalysisRepository.save(analysis);
                throw new RuntimeException("Analysis Lambda processing failed");
            }
            
            // 5. 분석 결과 파싱
            AnalysisLambdaResponse analysisResponse = lambdaUtil.parseResponse(response, AnalysisLambdaResponse.class);
            
            if (!analysisResponse.isSuccess() || analysisResponse.getData() == null) {
                log.warn("Analysis Lambda 처리 실패 - ContractId: {}, Message: {}", 
                        request.getContractId(), analysisResponse.getMessage());
                analysis.setStatus("failed");
                contractAnalysisRepository.save(analysis);
                throw new RuntimeException("Analysis processing failed: " + analysisResponse.getMessage());
            }
            
            // 6. 분석 결과를 DB에 저장
            saveAnalysisResults(analysis, analysisResponse.getData());
            
            // 7. 응답 DTO 생성 및 반환
            return createAnalysisResultResponse(request.getContractId(), analysis, ocrContents);
            
        } catch (Exception e) {
            log.error("분석 처리 실패 - ContractId: {}, AnalysisId: {}", request.getContractId(), analysisId, e);
            analysis.setStatus("failed");
            contractAnalysisRepository.save(analysis);
            throw new RuntimeException("Failed to process analysis", e);
        }
    }
    
    /**
     * 분석 결과를 DB에 저장
     */
    private void saveAnalysisResults(ContractAnalysis analysis, AnalysisLambdaResponse.AnalysisData data) {
        // ContractAnalysis 업데이트
        analysis.setSummary(data.getSummary());
        analysis.setDdobakOverallComment(data.getDdobakCommentary().getOverallComment());
        analysis.setDdobakWarningComment(data.getDdobakCommentary().getWarningComment());
        analysis.setDdobakAdvice(data.getDdobakCommentary().getAdvice());
        analysis.setStatus("completed");
        contractAnalysisRepository.save(analysis);
        
        // 독소 조항 저장
        for (AnalysisLambdaResponse.ToxicClause lambdaToxic : data.getToxics()) {
            ToxicClause toxicClause = new ToxicClause(
                IdGenerator.generate(),
                analysis.getId(),
                lambdaToxic.getTitle(), // title - 기본값 설정, 필요시 Lambda 응답에서 받아올 수 있음
                lambdaToxic.getClause(),
                lambdaToxic.getReason(),
                lambdaToxic.getReasonReference(),
                null, // sourceContractTagIdx - 필요시 추후 매핑
                lambdaToxic.getWarnLevel()
            );
            toxicClauseRepository.save(toxicClause);
        }
        
        log.info("분석 결과 저장 완료 - AnalysisId: {}, ToxicClauses: {}", 
                analysis.getId(), data.getToxics().size());
    }
    
    /**
     * 분석 결과 응답 DTO 생성
     */
    private AnalysisResultResponse createAnalysisResultResponse(String contractId, ContractAnalysis analysis, List<OcrContent> ocrContents) {
        // OCR 원본 내용 조합
        StringBuilder originContent = new StringBuilder();
        for (OcrContent content : ocrContents) {
            originContent.append(content.getContent());
        }
        
        // 독소 조항 조회 및 변환
        List<ToxicClause> toxicClauses = toxicClauseRepository.findByAnalysisIdOrderByWarnLevelDesc(analysis.getId());
        List<AnalysisResultResponse.ToxicClauseDto> toxics = new ArrayList<>();
        for (ToxicClause toxicClause : toxicClauses) {
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
        response.setToxicCount(toxicClauses.size());
        response.setDdobakCommentary(new AnalysisResultResponse.DdobakCommentary(
            analysis.getDdobakOverallComment(),
            analysis.getDdobakWarningComment(),
            analysis.getDdobakAdvice()
        ));
        response.setToxics(toxics);
        
        return response;
    }

    /**
     * 분석 결과 조회
     */
    @Transactional(readOnly = true)
    public AnalysisResultResponse getAnalysisResult(String contractId, String analysisId) {
        // 분석 결과 조회
        ContractAnalysis analysis = contractAnalysisRepository.findByIdWithToxicClauses(analysisId)
            .orElseThrow(() -> new RuntimeException("Analysis not found"));
        
        if (!analysis.getContractId().equals(contractId)) {
            throw new RuntimeException("Contract ID mismatch");
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
                "독소조항", // title - 실제로는 조항 유형에 따라 다를 수 있음
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
} 