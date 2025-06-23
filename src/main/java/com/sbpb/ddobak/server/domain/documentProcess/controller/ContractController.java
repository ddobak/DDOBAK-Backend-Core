package com.sbpb.ddobak.server.domain.documentProcess.controller;

import com.sbpb.ddobak.server.common.response.ApiResponse;
import com.sbpb.ddobak.server.domain.documentProcess.dto.*;
import com.sbpb.ddobak.server.domain.documentProcess.exception.DocumentProcessSuccessCode;
import com.sbpb.ddobak.server.domain.documentProcess.service.application.DocumentProcessApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 문서 처리 컨트롤러
 * 새로운 OCR 분리 및 병렬 처리 요구사항에 맞춰 개편
 */
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Slf4j
public class ContractController {

    private final DocumentProcessApplicationService documentProcessApplicationService;

    /**
     * 전체 문서 처리 API (OCR + 자동 분석 요청)
     * POST /api/v1/documents/process
     */
    @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentProcessResponse>> processDocumentComplete(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "contractType", required = false) String contractType,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        // 임시로 고정 사용자 ID 사용 (실제로는 JWT에서 추출)
        String userId = "UTEST001";
        
        log.info("Complete document processing request - userId: {}, fileCount: {}, contractType: {}", 
                userId, files.size(), contractType);

        DocumentProcessResponse response = documentProcessApplicationService
                .processDocumentComplete(userId, files, contractType);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, DocumentProcessSuccessCode.ANALYSIS_SUCCESS));
    }

    /**
     * OCR만 처리 API
     * POST /api/v1/documents/ocr
     */
    @PostMapping(value = "/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<OcrResponse>> processOcrOnly(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "contractType", required = false) String contractType,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        // 임시로 고정 사용자 ID 사용 (실제로는 JWT에서 추출)
        String userId = "UTEST001";
        
        log.info("OCR processing request - userId: {}, fileCount: {}, contractType: {}", 
                userId, files.size(), contractType);

        OcrResponse response = documentProcessApplicationService
                .processOcrOnly(userId, files, contractType);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, DocumentProcessSuccessCode.ANALYSIS_SUCCESS));
    }

    /**
     * OCR 컨텐츠 수정 API
     * PUT /api/v1/documents/{contractId}/ocr
     */
    @PutMapping("/{contractId}/ocr")
    public ResponseEntity<ApiResponse<Void>> updateOcrContent(
            @PathVariable String contractId,
            @Valid @RequestBody OcrContentUpdateRequest request) {

        log.info("OCR content update request - contractId: {}, pageIndex: {}", 
                contractId, request.getPageIndex());

        // 요청 DTO에 contractId 설정 (URL에서 가져온 값으로 덮어쓰기)
        OcrContentUpdateRequest updatedRequest = OcrContentUpdateRequest.builder()
                .contractId(contractId)
                .pageIndex(request.getPageIndex())
                .content(request.getContent())
                .build();

        documentProcessApplicationService.updateOcrContent(updatedRequest);

        return ResponseEntity.ok(ApiResponse.success(null, DocumentProcessSuccessCode.ANALYSIS_SUCCESS));
    }

    /**
     * 분석 요청 API
     * POST /api/v1/documents/{contractId}/analysis
     */
    @PostMapping("/{contractId}/analysis")
    public ResponseEntity<ApiResponse<AnalysisResponse>> requestAnalysis(
            @PathVariable String contractId) {

        log.info("Analysis request - contractId: {}", contractId);

        AnalysisRequest request = AnalysisRequest.builder()
                .contractId(contractId)
                .build();

        AnalysisResponse response = documentProcessApplicationService.requestAnalysis(request);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(ApiResponse.success(response, DocumentProcessSuccessCode.ANALYSIS_SUCCESS));
    }

    /**
     * 분석 결과 조회 API
     * GET /api/v1/documents/{contractId}/analysis
     */
    @GetMapping("/{contractId}/analysis")
    public ResponseEntity<ApiResponse<AnalysisResponse>> getAnalysisResult(
            @PathVariable String contractId) {

        log.info("Analysis result request - contractId: {}", contractId);

        AnalysisResponse response = documentProcessApplicationService.getAnalysisResult(contractId);

        return ResponseEntity.ok(ApiResponse.success(response, DocumentProcessSuccessCode.ANALYSIS_SUCCESS));
    }

    /**
     * OCR 결과 조회 API
     * GET /api/v1/documents/{contractId}/ocr
     */
    @GetMapping("/{contractId}/ocr")
    public ResponseEntity<ApiResponse<List<OcrResponse.OcrResult>>> getOcrResults(
            @PathVariable String contractId) {

        log.info("OCR results request - contractId: {}", contractId);

        List<OcrResponse.OcrResult> response = documentProcessApplicationService.getOcrResults(contractId);

        return ResponseEntity.ok(ApiResponse.success(response, DocumentProcessSuccessCode.ANALYSIS_SUCCESS));
    }

    /**
     * 전체 처리 상태 조회 API
     * GET /api/v1/documents/{contractId}/status
     */
    @GetMapping("/{contractId}/status")
    public ResponseEntity<ApiResponse<DocumentProcessStatusResponse>> getProcessStatus(
            @PathVariable String contractId) {

        log.info("Process status request - contractId: {}", contractId);

        DocumentProcessStatusResponse response = documentProcessApplicationService.getProcessStatus(contractId);

        return ResponseEntity.ok(ApiResponse.success(response, DocumentProcessSuccessCode.ANALYSIS_SUCCESS));
    }
} 