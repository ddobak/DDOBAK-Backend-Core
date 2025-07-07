package com.sbpb.ddobak.server.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserAnalysesRequest {
    
    private int requestCount = 10; // 기본값 설정
    
    public UserAnalysesRequest(int requestCount) {
        this.requestCount = requestCount;
    }
}