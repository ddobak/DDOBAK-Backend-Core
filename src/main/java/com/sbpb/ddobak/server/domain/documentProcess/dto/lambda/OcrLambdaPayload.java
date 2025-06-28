package com.sbpb.ddobak.server.domain.documentProcess.dto.lambda;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OCR Lambda 호출을 위한 페이로드
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OcrLambdaPayload {
    
    private String s3Key;
    private int pageIdx;
} 