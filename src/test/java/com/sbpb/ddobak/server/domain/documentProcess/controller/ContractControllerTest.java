package com.sbpb.ddobak.server.domain.documentProcess.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbpb.ddobak.server.domain.documentProcess.dto.*;
import com.sbpb.ddobak.server.domain.documentProcess.service.application.DocumentProcessApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ContractController 테스트
 */
@WebMvcTest(ContractController.class)
@DisplayName("문서 처리 Controller 테스트")
class ContractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentProcessApplicationService documentProcessApplicationService;

    @Autowired
    private ObjectMapper objectMapper;

    private String contractId;
    private MockMultipartFile testFile1;
    private MockMultipartFile testFile2;

    @BeforeEach
    void setUp() {
        contractId = "CONTRACT001";
        testFile1 = new MockMultipartFile("files", "contract1.jpg", "image/jpeg", "test content 1".getBytes());
        testFile2 = new MockMultipartFile("files", "contract2.jpg", "image/jpeg", "test content 2".getBytes());
    }

    @Test
    @DisplayName("OCR 처리 API 성공")
    void processOcr_Success() throws Exception {
        // given
        OcrResponse ocrResponse = OcrResponse.builder()
                .contractId(contractId)
                .ocrResults(List.of(
                    OcrResponse.OcrResult.builder()
                            .ocrContentId("OCR001")
                            .pageIndex(0)
                            .htmlContent("<html>Content 1</html>")
                            .build(),
                    OcrResponse.OcrResult.builder()
                            .ocrContentId("OCR002")
                            .pageIndex(1)
                            .htmlContent("<html>Content 2</html>")
                            .build()
                ))
                .build();

        given(documentProcessApplicationService.processOcr(anyString(), any(), anyString()))
                .willReturn(ocrResponse);

        // when & then
        mockMvc.perform(multipart("/api/v1/documents/ocr")
                .file(testFile1)
                .file(testFile2)
                .param("contractType", "employment"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.contractId").value(contractId))
                .andExpect(jsonPath("$.data.ocrResults").isArray())
                .andExpect(jsonPath("$.data.ocrResults.length()").value(2));

        verify(documentProcessApplicationService).processOcr(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("OCR 컨텐츠 수정 API 성공")
    void updateOcrContent_Success() throws Exception {
        // given
        OcrContentUpdateRequest request = OcrContentUpdateRequest.builder()
                .pageIndex(0)
                .content("<html>Updated Content</html>")
                .build();

        // when & then
        mockMvc.perform(put("/api/v1/documents/{contractId}/ocr", contractId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(documentProcessApplicationService).updateOcrContent(any(OcrContentUpdateRequest.class));
    }

    @Test
    @DisplayName("분석 요청 API 성공")
    void requestAnalysis_Success() throws Exception {
        // given
        AnalysisResponse analysisResponse = AnalysisResponse.builder()
                .contractId(contractId)
                .analysisId("ANALYSIS001")
                .status("REQUESTED")
                .message("Analysis request submitted successfully")
                .build();

        given(documentProcessApplicationService.requestAnalysis(any(AnalysisRequest.class)))
                .willReturn(analysisResponse);

        // when & then
        mockMvc.perform(post("/api/v1/documents/{contractId}/analysis", contractId))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.contractId").value(contractId))
                .andExpect(jsonPath("$.data.status").value("REQUESTED"));

        verify(documentProcessApplicationService).requestAnalysis(any(AnalysisRequest.class));
    }

    @Test
    @DisplayName("분석 결과 조회 API 성공")
    void getAnalysisResult_Success() throws Exception {
        // given
        AnalysisResponse analysisResponse = AnalysisResponse.builder()
                .contractId(contractId)
                .analysisId("ANALYSIS001")
                .status("COMPLETED")
                .message("Analysis completed successfully")
                .build();

        given(documentProcessApplicationService.getAnalysisResult(contractId))
                .willReturn(analysisResponse);

        // when & then
        mockMvc.perform(get("/api/v1/documents/{contractId}/analysis", contractId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.contractId").value(contractId))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        verify(documentProcessApplicationService).getAnalysisResult(contractId);
    }

    @Test
    @DisplayName("OCR 결과 조회 API 성공")
    void getOcrResults_Success() throws Exception {
        // given
        List<OcrResponse.OcrResult> ocrResults = List.of(
            OcrResponse.OcrResult.builder()
                    .ocrContentId("OCR001")
                    .pageIndex(0)
                    .htmlContent("<html>Content 1</html>")
                    .build(),
            OcrResponse.OcrResult.builder()
                    .ocrContentId("OCR002")
                    .pageIndex(1)
                    .htmlContent("<html>Content 2</html>")
                    .build()
        );

        given(documentProcessApplicationService.getOcrResults(contractId))
                .willReturn(ocrResults);

        // when & then
        mockMvc.perform(get("/api/v1/documents/{contractId}/ocr", contractId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(documentProcessApplicationService).getOcrResults(contractId);
    }

    @Test
    @DisplayName("처리 상태 조회 API 성공")
    void getProcessStatus_Success() throws Exception {
        // given
        DocumentProcessStatusResponse statusResponse = DocumentProcessStatusResponse.builder()
                .contractId(contractId)
                .ocrStatus("COMPLETED")
                .analysisStatus("COMPLETED")
                .processStatus("COMPLETED")
                .overallStatus("ALL_COMPLETED")
                .totalPages(2)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .statusMessage("All processing completed successfully")
                .build();

        given(documentProcessApplicationService.getProcessStatus(contractId))
                .willReturn(statusResponse);

        // when & then
        mockMvc.perform(get("/api/v1/documents/{contractId}/status", contractId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.contractId").value(contractId))
                .andExpect(jsonPath("$.data.ocrStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.analysisStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.processStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.overallStatus").value("ALL_COMPLETED"));

        verify(documentProcessApplicationService).getProcessStatus(contractId);
    }
} 