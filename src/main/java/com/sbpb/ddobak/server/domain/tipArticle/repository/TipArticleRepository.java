package com.sbpb.ddobak.server.domain.tipArticle.repository;

import com.sbpb.ddobak.server.domain.tipArticle.entity.TipArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 꿀팁 아티클 Repository
 */
@Repository
public interface TipArticleRepository extends JpaRepository<TipArticle, Long> {

    /**
     * 모든 꿀팁 아티클 목록 조회 (생성일시 기준 내림차순)
     */
    List<TipArticle> findAllByOrderByCreatedAtDesc();
} 