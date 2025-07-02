package com.sbpb.ddobak.server.domain.documentProcess.repository;

import com.sbpb.ddobak.server.domain.documentProcess.entity.ToxicClause;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToxicClauseRepository extends JpaRepository<ToxicClause, String> {

    /**
     * 분석 ID로 독소 조항 목록 조회 (위험 레벨 내림차순)
     */
    List<ToxicClause> findByAnalysisIdOrderByWarnLevelDesc(String analysisId);

    /**
     * 분석 ID와 위험 레벨로 독소 조항 목록 조회
     */
    List<ToxicClause> findByAnalysisIdAndWarnLevelGreaterThanEqual(String analysisId, Integer minWarnLevel);

    /**
     * 분석 ID로 독소 조항 개수 조회
     */
    long countByAnalysisId(String analysisId);

    /**
     * 분석 ID와 위험 레벨로 독소 조항 개수 조회
     */
    long countByAnalysisIdAndWarnLevelGreaterThanEqual(String analysisId, Integer minWarnLevel);
} 