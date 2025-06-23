package com.sbpb.ddobak.server.domain.documentProcess.service.infrastructure.impl;

import com.sbpb.ddobak.server.common.response.ApiResponse;
import com.sbpb.ddobak.server.domain.documentProcess.service.infrastructure.ExternalAnalysisService;
import com.sbpb.ddobak.server.infrastructure.aws.lambda.LambdaInvoker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 외부 분석 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalAnalysisServiceImpl implements ExternalAnalysisService {

    private final LambdaInvoker lambdaInvoker;

    @Override
    public String requestContractAnalysis(String contractId, List<String> ocrContents) {
        try {
            log.info("Requesting contract analysis for contract: {}", contractId);

            // 1. 분석 Lambda 요청 생성
            Map<String, Object> analysisRequest = createAnalysisLambdaRequest(contractId, ocrContents);
            
            // 2. Lambda 분석 함수 호출
            ApiResponse<Map<String, Object>> analysisResponse = lambdaInvoker.invoke("analysis-function", analysisRequest);
            
            // 3. 분석 요청 ID 추출
            String analysisRequestId = extractAnalysisRequestId(analysisResponse);
            
            log.info("Contract analysis requested successfully: contractId={}, requestId={}", contractId, analysisRequestId);
            
            return analysisRequestId;
            
        } catch (Exception e) {
            log.error("Failed to request contract analysis for contract: {}: {}", contractId, e.getMessage(), e);
            throw new RuntimeException("Analysis request failed", e);
        }
    }

    @Override
    public String getAnalysisResult(String contractId, String analysisRequestId) {
        try {
            log.info("Retrieving analysis result for contract: {}, requestId: {}", contractId, analysisRequestId);

            // 1. 분석 결과 조회 요청 생성
            Map<String, Object> resultRequest = createResultLambdaRequest(contractId, analysisRequestId);
            
            // 2. Lambda 결과 조회 함수 호출
            ApiResponse<Map<String, Object>> resultResponse = lambdaInvoker.invoke("analysis-result-function", resultRequest);
            
            // 3. 분석 결과 추출
            String analysisResult = extractAnalysisResult(resultResponse);
            
            log.info("Analysis result retrieved successfully for contract: {}", contractId);
            
            return analysisResult;
            
        } catch (Exception e) {
            log.error("Failed to get analysis result for contract: {}: {}", contractId, e.getMessage(), e);
            throw new RuntimeException("Analysis result retrieval failed", e);
        }
    }

    /**
     * 분석 Lambda 요청 Map 생성
     */
    private Map<String, Object> createAnalysisLambdaRequest(String contractId, List<String> ocrContents) {
        return Map.of(
            "contractId", contractId,
            "ocrContents", ocrContents,
            "analysisType", "contract_review",
            "timestamp", System.currentTimeMillis()
        );
    }

    /**
     * 결과 조회 Lambda 요청 Map 생성
     */
    private Map<String, Object> createResultLambdaRequest(String contractId, String analysisRequestId) {
        return Map.of(
            "contractId", contractId,
            "analysisRequestId", analysisRequestId,
            "timestamp", System.currentTimeMillis()
        );
    }

    /**
     * 분석 응답에서 요청 ID 추출
     */
    private String extractAnalysisRequestId(ApiResponse<Map<String, Object>> analysisResponse) {
        if (analysisResponse.isSuccess() && analysisResponse.getData() != null) {
            Map<String, Object> data = analysisResponse.getData();
            return data.getOrDefault("analysisRequestId", "").toString();
        }
        
        return "";
    }

    /**
     * 분석 결과 응답에서 결과 추출
     */
    private String extractAnalysisResult(ApiResponse<Map<String, Object>> resultResponse) {
        if (resultResponse.isSuccess() && resultResponse.getData() != null) {
            Map<String, Object> data = resultResponse.getData();
            return data.getOrDefault("analysisResult", "").toString();
        }
        
        return "";
    }
} 