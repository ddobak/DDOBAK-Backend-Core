package com.sbpb.ddobak.server.domain.tipArticle.controller;

import com.sbpb.ddobak.server.common.response.ApiResponse;
import com.sbpb.ddobak.server.domain.tipArticle.dto.TipArticleDetailResponse;
import com.sbpb.ddobak.server.domain.tipArticle.dto.TipArticleListResponse;
import com.sbpb.ddobak.server.domain.tipArticle.response.TipArticleSuccessCode;
import com.sbpb.ddobak.server.domain.tipArticle.service.TipArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 꿀팁 아티클 API 컨트롤러
 * 공개 API (인증 불필요)
 */
@RestController
@RequestMapping("/api/tips")
@RequiredArgsConstructor
@Slf4j
public class TipArticleController {

    private final TipArticleService tipArticleService;

    /**
     * 꿀팁 아티클 목록 조회
     * GET /api/tips
     * 
     * @return 꿀팁 아티클 목록 (content 제외)
     */
    @GetMapping
    public ApiResponse<List<TipArticleListResponse>> getAllTipArticles() {
        log.info("꿀팁 아티클 목록 조회 요청");
        
        List<TipArticleListResponse> responses = tipArticleService.getAllTipArticles();
        
        return ApiResponse.success(responses, TipArticleSuccessCode.TIP_ARTICLE_LIST_RETRIEVED);
    }

    /**
     * 꿀팁 아티클 상세 조회
     * GET /api/tips/{id}
     * 
     * @param id 꿀팁 아티클 ID
     * @return 꿀팁 아티클 상세 정보 (content 포함)
     */
    @GetMapping("/{id}")
    public ApiResponse<TipArticleDetailResponse> getTipArticleDetail(@PathVariable Long id) {
        log.info("꿀팁 아티클 상세 조회 요청, ID: {}", id);
        
        TipArticleDetailResponse response = tipArticleService.getTipArticleDetail(id);
        
        return ApiResponse.success(response, TipArticleSuccessCode.TIP_ARTICLE_DETAIL_RETRIEVED);
    }
} 