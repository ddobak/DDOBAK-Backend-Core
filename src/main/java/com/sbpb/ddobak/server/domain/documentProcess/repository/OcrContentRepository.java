package com.sbpb.ddobak.server.domain.documentProcess.repository;

import com.sbpb.ddobak.server.domain.documentProcess.entity.OcrContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * OCR 컨텐츠 Repository
 */
@Repository
public interface OcrContentRepository extends JpaRepository<OcrContent, String> {

    /**
     * 계약서 ID로 OCR 컨텐츠 목록 조회 (tag_idx 순서로 정렬)
     */
    List<OcrContent> findByContractIdOrderByTagIdx(String contractId);

    /**
     * 계약서 ID와 tag_idx로 특정 OCR 컨텐츠 조회
     */
    Optional<OcrContent> findByContractIdAndTagIdx(String contractId, Integer tagIdx);

    /**
     * 계약서 ID로 OCR 컨텐츠 개수 조회
     */
    @Query("SELECT COUNT(o) FROM OcrContent o WHERE o.contractId = :contractId")
    Long countByContractId(@Param("contractId") String contractId);

    /**
     * 계약서 ID로 OCR 컨텐츠 모두 삭제
     */
    void deleteByContractId(String contractId);
} 