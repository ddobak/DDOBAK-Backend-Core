package com.sbpb.ddobak.server.domain.documentProcess.repository;

import com.sbpb.ddobak.server.domain.documentProcess.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, String> {

    /**
     * 사용자별 계약서 목록 조회 (최신순)
     */
    List<Contract> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 특정 사용자의 특정 계약서 조회
     */
    Optional<Contract> findByIdAndUserId(String id, Long userId);

    /**
     * 계약서와 OCR 내용을 함께 조회
     */
    @Query("SELECT c FROM Contract c LEFT JOIN FETCH c.ocrContents WHERE c.id = :contractId")
    Optional<Contract> findByIdWithOcrContents(@Param("contractId") String contractId);

    /**
     * 계약서와 분석 결과를 함께 조회
     */
    @Query("SELECT c FROM Contract c LEFT JOIN FETCH c.contractAnalyses WHERE c.id = :contractId")
    Optional<Contract> findByIdWithAnalyses(@Param("contractId") String contractId);
} 