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
 * ContractController REST API 테스트
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

    private MockMultipartFile testFile1;
    private MockMultipartFile testFile2;

    @BeforeEach
    void setUp() {
        testFile1 = new MockMultipartFile("files", "contract1.jpg", "image/jpeg", "test content 1".getBytes());
        testFile2 = new MockMultipartFile("files", "contract2.jpg", "image/jpeg", "test content 2".getBytes());
    }

    @Test
    @DisplayName("전체 문서 처리 API 테스트")
    void processDocumentComplete_Success() throws Exception {
        // given
        DocumentProcessResponse mockResponse = DocumentProcessResponse.builder()
                .contractId("CONTRACT001")
                .ocrResults(List.of(
                    OcrResponse.OcrResult.builder()
                            .ocrContentId("OCR001")
                            .pageIndex(0)
                            .htmlContent("<html>Content 1</html>")
                            .build()
                ))
                .analysisInfo(AnalysisResponse.builder()
                        .contractId("CONTRACT001")
                        .analysisId("ANALYSIS001")
                        .status("REQUESTED")
                        .message("Analysis request submitted successfully")
                        .build())
                .processingStatus("OCR_COMPLETED_ANALYSIS_REQUESTED")
                .message("Document processing completed successfully")
                .build();

        given(documentProcessApplicationService.processDocumentComplete(anyString(), any(), anyString()))
                .willReturn(mockResponse);

        // when & then
        mockMvc.perform(multipart("/api/v1/documents/process")
                        .file(testFile1)
                        .file(testFile2)
                        .param("contractType", "employment"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.contractId").value("CONTRACT001"))
                .andExpect(jsonPath("$.data.ocrResults").isArray())
                .andExpect(jsonPath("$.data.ocrResults[0].pageIndex").value(0))
                .andExpect(jsonPath("$.data.analysisInfo.status").value("REQUESTED"));

        verify(documentProcessApplicationService).processDocumentComplete(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("OCR만 처리 API 테스트")
    void processOcrOnly_Success() throws Exception {
        // given
        OcrResponse mockResponse = OcrResponse.builder()
                .contractId("CONTRACT001")
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

        given(documentProcessApplicationService.processOcrOnly(anyString(), any(), anyString()))
                .willReturn(mockResponse);

        // when & then
        mockMvc.perform(multipart("/api/v1/documents/ocr")
                        .file(testFile1)
                        .file(testFile2)
                        .param("contractType", "employment"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.contractId").value("CONTRACT001"))
                .andExpect(jsonPath("$.data.ocrResults").isArray());

        verify(documentProcessApplicationService).processOcrOnly(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("분석 요청 API 테스트")
    void requestAnalysis_Success() throws Exception {
        // given
        String contractId = "CONTRACT001";
        AnalysisResponse mockResponse = AnalysisResponse.builder()
                .contractId(contractId)
                .analysisId("ANALYSIS001")
                .status("REQUESTED")
                .message("Analysis request submitted successfully")
                .build();

        given(documentProcessApplicationService.requestAnalysis(any(AnalysisRequest.class)))
                .willReturn(mockResponse);

        // when & then
        mockMvc.perform(post("/api/v1/documents/{contractId}/analysis", contractId))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.contractId").value(contractId))
                .andExpect(jsonPath("$.data.status").value("REQUESTED"));

        verify(documentProcessApplicationService).requestAnalysis(any(AnalysisRequest.class));
    }
} 