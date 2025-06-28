package com.sbpb.ddobak.server.domain.documentProcess.dto.analysis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRequest {
    
    private String contractId;
    private boolean ocrSucceeded;
} 