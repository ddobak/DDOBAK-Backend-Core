package com.sbpb.ddobak.server.domain.documentProcess.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * OCR 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OcrResponse {

    /**
     * 생성된 계약서 ID
     */
    private String contractId;

    /**
     * OCR 결과 목록 (페이지별)
     */
    private List<OcrResult> ocrResults;

    /**
     * OCR 결과 상세 정보
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OcrResult {
        /**
         * OCR 컨텐츠 ID
         */
        private String ocrContentId;

        /**
         * 페이지 순서 (tag_idx)
         */
        private Integer pageIndex;

        /**
         * OCR 추출된 HTML 텍스트
         */
        private String htmlContent;
    }
} 