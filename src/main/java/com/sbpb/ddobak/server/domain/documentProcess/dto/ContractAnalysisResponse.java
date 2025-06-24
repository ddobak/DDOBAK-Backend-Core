package com.sbpb.ddobak.server.domain.documentProcess.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 계약서 분석 응답 DTO
 * Step Functions 완료 후 전체 분석 결과 포함
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractAnalysisResponse {

    /**
     * 계약서 ID
     */
    private String contractId;
    
    /**
     * 처리된 이미지 S3 키 목록
     */
    private List<String> s3Keys;
    
    /**
     * 클라이언트 ID (요청시와 동일)
     */
    private String clientId;
    
    /**
     * 클라이언트 토큰 (요청시와 동일)
     */
    private String clientToken;
    
    /**
     * 예상 페이지 수 (요청시와 동일)
     */
    private Integer expectedCount;
    
    /**
     * OCR 처리 결과 목록
     */
    private List<OcrResult> ocrResults;
    
    /**
     * Bedrock AI 분석 결과
     */
    private BedrockAnalysisResult bedrockResults;
    
    // ===== 간단한 응답용 생성자 (즉시 응답용) =====
    
    /**
     * 즉시 응답용 간단한 응답 생성
     * Step Functions 실행 시작 후 contractId만 반환할 때 사용
     */
    public static ContractAnalysisResponse createSimpleResponse(String contractId) {
        return ContractAnalysisResponse.builder()
                .contractId(contractId)
                .build();
    }
    
    /**
     * 즉시 응답용 클라이언트 정보 포함 응답 생성
     */
    public static ContractAnalysisResponse createSimpleResponse(String contractId, String clientId, String clientToken, Integer expectedCount) {
        return ContractAnalysisResponse.builder()
                .contractId(contractId)
                .clientId(clientId)
                .clientToken(clientToken)
                .expectedCount(expectedCount)
                .build();
    }
} 