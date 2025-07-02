package com.sbpb.ddobak.server.domain.documentProcess.dto.ocr;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OcrContentResponse {
    
    private int pageIdx;
    private String htmlEntire;
    private List<HtmlElement> htmlArray;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HtmlElement {
        private String category;
        private String element;
        private int tagIdx;
    }
} 