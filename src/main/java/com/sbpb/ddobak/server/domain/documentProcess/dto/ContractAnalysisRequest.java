package com.sbpb.ddobak.server.domain.documentProcess.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 계약서 분석 요청 DTO
 * 다중 페이지 이미지 처리 지원
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractAnalysisRequest {

    /**
     * 계약서 이미지 파일 목록 (다중 페이지 지원)
     */
    private List<MultipartFile> files;
    
    /**
     * 계약서 타입 (GENERAL, EMPLOYMENT, RENTAL 등)
     */
    private String contractType;
    
    /**
     * 클라이언트 ID (향후 푸시 알림용)
     */
    private String clientId;
    
    /**
     * 클라이언트 토큰 (FCM/APNS 토큰, 향후 푸시 알림용)
     */
    private String clientToken;
    
    /**
     * 예상 페이지 수 (검증용)
     */
    private Integer expectedCount;
} 