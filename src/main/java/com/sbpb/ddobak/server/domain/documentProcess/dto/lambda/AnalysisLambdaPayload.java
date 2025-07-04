package com.sbpb.ddobak.server.domain.documentProcess.dto.lambda;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Analysis Lambda 호출을 위한 페이로드
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisLambdaPayload {
    private String contractId;
    private String analysisId;
    
    private List<String> contractTexts;
} 