package com.sbpb.ddobak.server.domain.tipArticle.dto;

import com.sbpb.ddobak.server.domain.tipArticle.entity.TipArticle;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 꿀팁 아티클 상세 응답 DTO
 * content 포함
 */
@Getter
@Builder
public class TipArticleDetailResponse {

    private Long id;
    private String title;
    private String summary;
    private String content;
    private List<String> tags;

    /**
     * TipArticle 엔티티를 TipArticleDetailResponse로 변환
     */
    public static TipArticleDetailResponse from(TipArticle tipArticle) {
        return TipArticleDetailResponse.builder()
                .id(tipArticle.getId())
                .title(tipArticle.getTitle())
                .summary(tipArticle.getSummary())
                .content(tipArticle.getContent())
                .tags(tipArticle.getTags())
                .build();
    }
} 