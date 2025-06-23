package com.sbpb.ddobak.server.domain.documentProcess.repository;

import com.sbpb.ddobak.server.domain.documentProcess.entity.ContractAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 계약서 분석 Repository
 */
@Repository
public interface ContractAnalysisRepository extends JpaRepository<ContractAnalysis, String> {

    /**
     * 계약서 ID로 분석 결과 조회
     */
    Optional<ContractAnalysis> findByContractId(String contractId);

    /**
     * 계약서 ID로 분석 결과 존재 여부 확인
     */
    boolean existsByContractId(String contractId);

    /**
     * 계약서 ID로 분석 결과 삭제
     */
    void deleteByContractId(String contractId);
} 