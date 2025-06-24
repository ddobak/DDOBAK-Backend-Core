package com.sbpb.ddobak.server.domain.documentProcess.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 독소 조항 분석 결과 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToxicClause {

    /**
     * 독소 조항 제목
     */
    private String title;
    
    /**
     * 해당 조항 내용
     */
    private String clause;
    
    /**
     * 독소 판단 이유
     */
    private String reason;
    
    /**
     * 법적 근거 참조
     */
    private String reasonReference;
    
    /**
     * 경고 수준 (1: 낮음, 2: 보통, 3: 높음)
     */
    private Integer warnLevel;
} 