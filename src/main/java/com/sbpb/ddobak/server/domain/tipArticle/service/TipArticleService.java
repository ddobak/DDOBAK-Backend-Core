package com.sbpb.ddobak.server.domain.tipArticle.service;

import com.sbpb.ddobak.server.common.exception.ResourceNotFoundException;
import com.sbpb.ddobak.server.domain.tipArticle.dto.TipArticleDetailResponse;
import com.sbpb.ddobak.server.domain.tipArticle.dto.TipArticleListResponse;
import com.sbpb.ddobak.server.domain.tipArticle.entity.TipArticle;
import com.sbpb.ddobak.server.domain.tipArticle.repository.TipArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 꿀팁 아티클 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TipArticleService {

    private final TipArticleRepository tipArticleRepository;

    /**
     * 꿀팁 아티클 목록 조회
     */
    public List<TipArticleListResponse> getAllTipArticles() {
        log.info("꿀팁 아티클 목록 조회 시작");
        
        List<TipArticle> tipArticles = tipArticleRepository.findAllByOrderByCreatedAtDesc();
        log.info("DB 조회 결과: {} 건", tipArticles.size());
        
        if (tipArticles.isEmpty()) {
            log.warn("꿀팁 아티클이 비어있습니다. DB 연결 또는 스키마를 확인하세요.");
        } else {
            log.info("첫 번째 아티클: ID={}, Title={}", tipArticles.get(0).getId(), tipArticles.get(0).getTitle());
        }
        
        List<TipArticleListResponse> responses = tipArticles.stream()
                .map(TipArticleListResponse::from)
                .collect(Collectors.toList());
        
        log.info("꿀팁 아티클 목록 조회 완료, 총 {}개", responses.size());
        return responses;
    }

    /**
     * 꿀팁 아티클 상세 조회
     */
    public TipArticleDetailResponse getTipArticleDetail(Long id) {
        log.info("꿀팁 아티클 상세 조회 시작, ID: {}", id);
        
        TipArticle tipArticle = tipArticleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("꿀팁 아티클을 찾을 수 없습니다. ID: " + id));
        
        TipArticleDetailResponse response = TipArticleDetailResponse.from(tipArticle);
        
        log.info("꿀팁 아티클 상세 조회 완료, ID: {}", id);
        return response;
    }
} 