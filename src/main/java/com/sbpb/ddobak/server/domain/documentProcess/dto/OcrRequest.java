package com.sbpb.ddobak.server.domain.documentProcess.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * OCR 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OcrRequest {

    /**
     * 업로드할 파일 목록
     */
    @NotNull(message = "Files are required")
    private List<MultipartFile> files;

    /**
     * 계약서 유형 (선택사항)
     */
    private String contractType;
} 