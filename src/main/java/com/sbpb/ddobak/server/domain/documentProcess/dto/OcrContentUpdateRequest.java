package com.sbpb.ddobak.server.domain.documentProcess.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * OCR 컨텐츠 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OcrContentUpdateRequest {

    /**
     * 계약서 ID
     */
    @NotBlank(message = "Contract ID is required")
    private String contractId;

    /**
     * 페이지 인덱스 (tag_idx)
     */
    @NotNull(message = "Page index is required")
    private Integer pageIndex;

    /**
     * 수정된 HTML 컨텐츠
     */
    @NotBlank(message = "Content is required")
    private String content;
} 