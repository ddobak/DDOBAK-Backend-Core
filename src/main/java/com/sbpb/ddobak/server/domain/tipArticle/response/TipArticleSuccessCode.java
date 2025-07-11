package com.sbpb.ddobak.server.domain.tipArticle.response;

import com.sbpb.ddobak.server.common.response.BaseSuccessCode;

/**
 * TipArticle 도메인 성공 코드 (9xxx 범위)
 */
public enum TipArticleSuccessCode implements BaseSuccessCode {

    // ===== 9000-9099: 꿀팁 아티클 조회 관련 =====
    TIP_ARTICLE_LIST_RETRIEVED(9000, "꿀팁 아티클 목록 조회 성공"),
    TIP_ARTICLE_DETAIL_RETRIEVED(9001, "꿀팁 아티클 상세 조회 성공");

    private final int code;
    private final String message;

    TipArticleSuccessCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getCodeAsString() {
        return String.valueOf(code);
    }
} 