package com.sbpb.ddobak.server.domain.documentProcess.dto.lambda;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * OCR Lambda 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class OcrLambdaResponse {
    
    private boolean success;
    private String message;
    private OcrData data;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class OcrData {
        @JsonProperty("page_idx")
        private int pageIdx;
        
        @JsonProperty("html_entire")
        private String htmlEntire;
        
        @JsonProperty("html_array")
        private List<HtmlElement> htmlArray;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class HtmlElement {
        private String category;
        private String html;
        private String id;
    }
} 