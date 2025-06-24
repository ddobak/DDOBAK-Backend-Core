package com.sbpb.ddobak.server.domain.documentProcess.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * OCR 처리 결과 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrResult {

    /**
     * 페이지 번호
     */
    private Integer page;
    
    /**
     * OCR로 추출된 텍스트
     */
    private String text;
    
    /**
     * 원본 이미지 S3 키
     */
    private String s3Key;
} 