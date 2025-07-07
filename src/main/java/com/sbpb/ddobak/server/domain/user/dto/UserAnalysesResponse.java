package com.sbpb.ddobak.server.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserAnalysesResponse {
    
    private int contractsCount;
    private List<ContractAnalysisDto> contracts;
    
    public UserAnalysesResponse(int contractsCount, List<ContractAnalysisDto> contracts) {
        this.contractsCount = contractsCount;
        this.contracts = contracts;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ContractAnalysisDto {
        private String contractId;
        private String contractTitle;
        private String contractType;
        private String analysisStatus;
        private String analysisId;
        private int toxicCounts;
        private String analysisDate;
        
        public ContractAnalysisDto(String contractId, String contractTitle, String contractType, 
                                 String analysisStatus, String analysisId, int toxicCounts, String analysisDate) {
            this.contractId = contractId;
            this.contractTitle = contractTitle;
            this.contractType = contractType;
            this.analysisStatus = analysisStatus;
            this.analysisId = analysisId;
            this.toxicCounts = toxicCounts;
            this.analysisDate = analysisDate;
        }
    }
}