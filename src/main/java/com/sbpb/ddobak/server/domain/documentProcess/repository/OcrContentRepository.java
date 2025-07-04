package com.sbpb.ddobak.server.domain.documentProcess.repository;

import com.sbpb.ddobak.server.domain.documentProcess.entity.OcrContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OcrContentRepository extends JpaRepository<OcrContent, String> {

    /**
     * 계약 ID로 OCR 콘텐츠 목록 조회 (태그 순서대로)
     */
    List<OcrContent> findByContractIdOrderByTagIdx(String contractId);

    /**
     * 계약 ID와 태그 인덱스로 OCR 콘텐츠 조회
     */
    Optional<OcrContent> findByContractIdAndTagIdx(String contractId, Integer tagIdx);

    /**
     * 계약 ID로 OCR 콘텐츠 개수 조회
     */
    long countByContractId(String contractId);
} 