package com.sbpb.ddobak.server.domain.documentProcess.dto.ocr;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OcrUpdateRequest {
    
    private String id;
    private String element;
} 