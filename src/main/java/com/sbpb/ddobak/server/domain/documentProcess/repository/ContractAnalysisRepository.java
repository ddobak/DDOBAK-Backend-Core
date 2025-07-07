package com.sbpb.ddobak.server.domain.documentProcess.repository;

import com.sbpb.ddobak.server.domain.documentProcess.entity.ContractAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractAnalysisRepository extends JpaRepository<ContractAnalysis, String> {

    /**
     * 계약 ID로 분석 결과 목록 조회 (최신순)
     */
    List<ContractAnalysis> findByContractIdOrderByCreatedAtDesc(String contractId);

    /**
     * 계약 ID와 분석 ID로 분석 결과 조회
     */
    Optional<ContractAnalysis> findByIdAndContractId(String id, String contractId);

    /**
     * 독소 조항과 함께 분석 결과 조회
     */
    @Query("SELECT ca FROM ContractAnalysis ca LEFT JOIN FETCH ca.toxicClauses WHERE ca.id = :analysisId")
    Optional<ContractAnalysis> findByIdWithToxicClauses(@Param("analysisId") String analysisId);

    /**
     * 계약 ID로 최신 분석 결과 조회
     */
    Optional<ContractAnalysis> findFirstByContractIdOrderByCreatedAtDesc(String contractId);
    
    /**
     * 사용자 ID로 최신 분석 결과들 조회 (Contract 정보 포함)
     */
    @Query("SELECT ca FROM ContractAnalysis ca " +
           "JOIN FETCH ca.contract c " +
           "WHERE c.userId = :userId " +
           "AND ca.createdAt = (SELECT MAX(ca2.createdAt) FROM ContractAnalysis ca2 WHERE ca2.contractId = ca.contractId) " +
           "ORDER BY ca.createdAt DESC")
    List<ContractAnalysis> findLatestAnalysesByUserId(@Param("userId") Long userId);
} 