package com.sbpb.ddobak.server.domain.tipArticle.dto;

import com.sbpb.ddobak.server.domain.tipArticle.entity.TipArticle;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 꿀팁 아티클 목록 응답 DTO
 * content는 포함하지 않음
 */
@Getter
@Builder
public class TipArticleListResponse {

    private Long id;
    private String title;
    private String summary;
    private List<String> tags;

    /**
     * TipArticle 엔티티를 TipArticleListResponse로 변환
     */
    public static TipArticleListResponse from(TipArticle tipArticle) {
        return TipArticleListResponse.builder()
                .id(tipArticle.getId())
                .title(tipArticle.getTitle())
                .summary(tipArticle.getSummary())
                .tags(tipArticle.getTags())
                .build();
    }
} 