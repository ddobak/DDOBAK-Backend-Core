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
     * 사용자 ID로 계약 목록 조회
     */
    List<Contract> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * 사용자 ID와 계약 ID로 계약 조회
     */
    Optional<Contract> findByIdAndUserId(String id, String userId);

    /**
     * OCR 콘텐츠와 함께 계약 조회
     */
    @Query("SELECT c FROM Contract c LEFT JOIN FETCH c.ocrContents WHERE c.id = :contractId")
    Optional<Contract> findByIdWithOcrContents(@Param("contractId") String contractId);

    /**
     * 분석 결과와 함께 계약 조회
     */
    @Query("SELECT c FROM Contract c LEFT JOIN FETCH c.contractAnalyses WHERE c.id = :contractId")
    Optional<Contract> findByIdWithAnalyses(@Param("contractId") String contractId);
} 