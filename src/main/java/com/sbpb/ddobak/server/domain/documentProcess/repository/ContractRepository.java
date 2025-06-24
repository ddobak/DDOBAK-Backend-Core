package com.sbpb.ddobak.server.domain.documentProcess.repository;

import com.sbpb.ddobak.server.domain.documentProcess.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 계약서 레포지토리
 */
@Repository
public interface ContractRepository extends JpaRepository<Contract, String> {

    /**
     * 사용자 ID로 계약서 목록 조회
     */
    List<Contract> findByUserId(String userId);

    /**
     * 계약서 ID와 사용자 ID로 조회 (권한 확인용)
     */
    Optional<Contract> findByIdAndUserId(String id, String userId);
} 