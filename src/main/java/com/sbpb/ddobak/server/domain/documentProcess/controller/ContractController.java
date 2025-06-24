package com.sbpb.ddobak.server.domain.documentProcess.controller;

import com.sbpb.ddobak.server.common.response.ApiResponse;
import com.sbpb.ddobak.server.domain.documentProcess.dto.*;
import com.sbpb.ddobak.server.domain.documentProcess.exception.DocumentProcessSuccessCode;
import com.sbpb.ddobak.server.domain.documentProcess.service.application.ContractApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 계약서 처리 컨트롤러
 */
@RestController
@RequestMapping("/api/contract")
@RequiredArgsConstructor
@Slf4j
public class ContractController {

    private final ContractApplicationService contractApplicationService;

    /**
     * 분석 요청 API
     * POST /api/contract/analysis
     */
    @PostMapping(value = "/analysis", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ContractAnalysisResponse>> processAnalysis(
            @ModelAttribute ContractAnalysisRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {

        // 임시로 고정 사용자 ID 사용 (실제로는 JWT에서 추출)
        String userId = "UTEST001";
        
        // 요청 정보 로깅
        int fileCount = request.getFiles() != null ? request.getFiles().size() : 0;
        log.info("Analysis request received - userId: {}, requestId: {}, fileCount: {}, clientId: {}", 
                userId, requestId, fileCount, request.getClientId());

        ContractAnalysisResponse response = contractApplicationService.processAnalysis(request, userId);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.success(response, DocumentProcessSuccessCode.ANALYSIS_SUCCESS));
    }
} 