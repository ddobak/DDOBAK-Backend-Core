package com.sbpb.ddobak.server.domain.documentProcess.service.domain;

import com.sbpb.ddobak.server.domain.documentProcess.dto.OcrResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * OCR 처리 도메인 서비스
 */
public interface OcrService {

    /**
     * 다중 파일 OCR 처리
     * 
     * @param userId 사용자 ID
     * @param files 업로드된 파일 목록
     * @param contractType 계약서 유형 (선택사항)
     * @return OCR 처리 결과
     */
    OcrResponse processMultipleFiles(String userId, List<MultipartFile> files, String contractType);

    /**
     * OCR 컨텐츠 업데이트
     * 
     * @param contractId 계약서 ID
     * @param pageIndex 페이지 인덱스
     * @param content 수정된 컨텐츠
     */
    void updateOcrContent(String contractId, Integer pageIndex, String content);

    /**
     * 계약서의 OCR 결과 조회
     * 
     * @param contractId 계약서 ID
     * @return OCR 결과 목록
     */
    List<OcrResponse.OcrResult> getOcrResults(String contractId);
} 