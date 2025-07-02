package com.sbpb.ddobak.server.domain.documentProcess.controller;

import com.sbpb.ddobak.server.common.response.ApiResponse;
import com.sbpb.ddobak.server.common.response.SuccessCode;
import com.sbpb.ddobak.server.domain.documentProcess.dto.ocr.*;
import com.sbpb.ddobak.server.domain.documentProcess.dto.analysis.*;
import com.sbpb.ddobak.server.domain.documentProcess.service.DocumentProcessService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/contract")
public class ContractController {

    private final DocumentProcessService documentProcessService;

    public ContractController(DocumentProcessService documentProcessService) {
        this.documentProcessService = documentProcessService;
    }

    /**
     * OCR 처리 요청
     * POST /contract/ocr
     */
    @PostMapping(value = "/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<OcrResponse> processOcr(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("contractType") String contractType/*,
            @RequestHeader("Authorization") String authorization*/) {
        
        // TODO: Authorization에서 사용자 ID 추출
        // String userId = extractUserIdFromToken(authorization);
        String userId = "user123";
        
        OcrRequest request = new OcrRequest(files, contractType);
        OcrResponse response = documentProcessService.processOcr(userId, request);
        
        return ApiResponse.success(response, SuccessCode.SUCCESS);
    }

    /**
     * OCR 결과 조회
     * GET /contract/ocr/{contractId}
     */
    @GetMapping("/ocr/{contractId}")
    public ApiResponse<OcrContentResponse> getOcrResults(
            @PathVariable("contractId") String contractId/*,
            @RequestHeader("Authorization") String authorization*/) {
        
        // TODO: 사용자 권한 검증
        
        OcrContentResponse response = documentProcessService.getOcrResults(contractId);
        return ApiResponse.success(response, SuccessCode.SUCCESS);
    }

    /**
     * OCR 내용 수정
     * PATCH /contract/ocr/{contractId}
     */
    @PatchMapping("/ocr/{contractId}")
    public ApiResponse<Void> updateOcrContent(
            @PathVariable("contractId") String contractId,
            @RequestBody OcrUpdateRequest request/*,
            @RequestHeader("Authorization") String authorization*/) {
        
        // TODO: 사용자 권한 검증
        
        documentProcessService.updateOcrContent(contractId, request);
        return ApiResponse.success(SuccessCode.SUCCESS);
    }

    /**
     * 분석 요청
     * POST /contract/analysis
     */
    @PostMapping("/analysis")
    public ApiResponse<AnalysisResponse> requestAnalysis(
            @RequestBody AnalysisRequest request/*,
            @RequestHeader("Authorization") String authorization*/) {
        
        // TODO: 사용자 권한 검증
        
        AnalysisResponse response = documentProcessService.requestAnalysis(request);
        return ApiResponse.success(response, SuccessCode.SUCCESS);
    }

    /**
     * 분석 결과 조회
     * GET /contract/{contractId}/analysis/{analysisId}
     */
    @GetMapping("/{contractId}/analysis/{analysisId}")
    public ApiResponse<AnalysisResultResponse> getAnalysisResult(
            @PathVariable("contractId") String contractId,
            @PathVariable("analysisId") String analysisId/*,
            @RequestHeader("Authorization") String authorization*/) {
        
        // TODO: 사용자 권한 검증
        
        AnalysisResultResponse response = documentProcessService.getAnalysisResult(contractId, analysisId);
        return ApiResponse.success(response, SuccessCode.SUCCESS);
    }

    /**
     * Authorization 헤더에서 사용자 ID 추출
     * TODO: 실제 JWT 토큰 파싱 로직 구현 필요
     */
    private String extractUserIdFromToken(String authorization) {
        // 임시로 더미 사용자 ID 반환
        return "user123";
    }
} 