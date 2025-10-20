package com.sbpb.ddobak.server.domain.documentProcess.controller;

import com.sbpb.ddobak.server.common.response.ApiResponse;
import com.sbpb.ddobak.server.common.response.SuccessCode;
import com.sbpb.ddobak.server.domain.documentProcess.dto.ocr.*;
import com.sbpb.ddobak.server.domain.documentProcess.dto.analysis.*;
import com.sbpb.ddobak.server.domain.documentProcess.service.DocumentProcessService;
import com.sbpb.ddobak.server.domain.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/contract")
@RequiredArgsConstructor
@Slf4j
public class ContractController {

    private final DocumentProcessService documentProcessService;
    private final JwtService jwtService;

    /**
     * OCR 처리 요청
     * POST /contract/ocr
     */
    @PostMapping(value = "/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<OcrResponse> processOcr(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("contractType") String contractType,
            @RequestHeader("Authorization") String authorization) {
        
        Long userId = extractUserIdFromToken(authorization);
        
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
            @PathVariable("contractId") String contractId,
            @RequestHeader("Authorization") String authorization) {
        
        Long userId = extractUserIdFromToken(authorization);
        
        OcrContentResponse response = documentProcessService.getOcrResults(contractId, userId);
        return ApiResponse.success(response, SuccessCode.SUCCESS);
    }

    /**
     * OCR 내용 수정
     * PATCH /contract/ocr/{contractId}
     */
    @PatchMapping("/ocr/{contractId}")
    public ApiResponse<Void> updateOcrContent(
            @PathVariable("contractId") String contractId,
            @RequestBody OcrUpdateRequest request,
            @RequestHeader("Authorization") String authorization) {
        
        Long userId = extractUserIdFromToken(authorization);
        
        documentProcessService.updateOcrContent(contractId, request, userId);
        return ApiResponse.success(SuccessCode.SUCCESS);
    }

    /**
     * 분석 요청
     * POST /contract/analysis
     */
    @PostMapping("/analysis")
    public ApiResponse<AnalysisResponse> requestAnalysis(
            @RequestBody AnalysisRequest request,
            @RequestHeader("Authorization") String authorization) {
        
        Long userId = extractUserIdFromToken(authorization);
        
        AnalysisResponse response = documentProcessService.requestAnalysis(request, userId);
        return ApiResponse.success(response, SuccessCode.SUCCESS);
    }

    /**
     * 분석 결과 조회
     * GET /contract/{contractId}/analysis/{analysisId}
     */
    @GetMapping("/{contractId}/analysis/{analysisId}")
    public ApiResponse<AnalysisResultResponse> getAnalysisResult(
            @PathVariable("contractId") String contractId,
            @PathVariable("analysisId") String analysisId,
            @RequestHeader("Authorization") String authorization) {

        Long userId = extractUserIdFromToken(authorization);

        AnalysisResultResponse response = documentProcessService.getAnalysisResult(contractId, analysisId, userId);
        return ApiResponse.success(response, SuccessCode.SUCCESS);
    }

    /**
     * 계약서 삭제
     * DELETE /contract/{contractId}
     */
    @DeleteMapping("/{contractId}")
    public ApiResponse<Void> deleteContract(
            @PathVariable("contractId") String contractId,
            @RequestHeader("Authorization") String authorization) {

        Long userId = extractUserIdFromToken(authorization);

        documentProcessService.deleteContract(contractId, userId);
        return ApiResponse.success(SuccessCode.SUCCESS);
    }

    /**
     * Authorization 헤더에서 사용자 ID 추출
     */
    private Long extractUserIdFromToken(String authorization) {
        log.debug("JWT 토큰에서 사용자 ID 추출 시작");
        
        try {
            // Bearer 토큰에서 실제 토큰 추출
            String accessToken = authorization.startsWith("Bearer ") 
                ? authorization.substring(7) 
                : authorization;
            
            // JWT 토큰 유효성 검증
            if (!jwtService.isTokenValid(accessToken)) {
                log.error("유효하지 않은 JWT 토큰입니다.");
                throw new IllegalArgumentException("유효하지 않은 JWT 토큰입니다.");
            }
            
            // JWT 토큰에서 사용자 ID 추출
            Long userId = jwtService.getUserIdFromToken(accessToken);
            log.debug("JWT 토큰에서 사용자 ID 추출 완료: {}", userId);
            
            return userId;
        } catch (Exception e) {
            log.error("JWT 토큰 파싱 중 오류 발생: {}", e.getMessage());
            throw new IllegalArgumentException("JWT 토큰 파싱 실패: " + e.getMessage());
        }
    }
} 