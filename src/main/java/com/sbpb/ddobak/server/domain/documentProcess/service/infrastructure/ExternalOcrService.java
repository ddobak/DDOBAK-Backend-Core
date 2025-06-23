package com.sbpb.ddobak.server.domain.documentProcess.service.infrastructure;

import org.springframework.web.multipart.MultipartFile;

/**
 * 외부 OCR 서비스 인터페이스 (Infrastructure Layer)
 */
public interface ExternalOcrService {

    /**
     * 파일에 대한 OCR 처리
     * 
     * @param file 처리할 파일
     * @param contractId 계약서 ID
     * @param pageIndex 페이지 인덱스
     * @return OCR 처리된 HTML 컨텐츠
     */
    String processOcrForFile(MultipartFile file, String contractId, Integer pageIndex);
} 