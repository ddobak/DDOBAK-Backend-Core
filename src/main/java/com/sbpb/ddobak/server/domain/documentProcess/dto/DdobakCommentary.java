package com.sbpb.ddobak.server.domain.documentProcess.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 또박이 코멘터리 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DdobakCommentary {

    /**
     * 또박이 전체 한마디
     */
    private String overallComment;
    
    /**
     * 주의사항 요약
     */
    private String warningComment;
    
    /**
     * 또박이의 조언
     */
    private String advice;
} 