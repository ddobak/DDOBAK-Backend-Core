package com.sbpb.ddobak.server.domain.documentProcess.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbpb.ddobak.server.domain.documentProcess.dto.analysis.AnalysisRequest;
import com.sbpb.ddobak.server.domain.documentProcess.dto.analysis.AnalysisResponse;
import com.sbpb.ddobak.server.domain.documentProcess.dto.analysis.AnalysisResultResponse;
import com.sbpb.ddobak.server.domain.documentProcess.dto.ocr.OcrContentResponse;
import com.sbpb.ddobak.server.domain.documentProcess.dto.ocr.OcrRequest;
import com.sbpb.ddobak.server.domain.documentProcess.dto.ocr.OcrResponse;
import com.sbpb.ddobak.server.domain.documentProcess.dto.ocr.OcrUpdateRequest;
import com.sbpb.ddobak.server.domain.documentProcess.service.DocumentProcessService;
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
@DisplayName("계약서 Controller 테스트")
class ContractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentProcessService documentProcessService;

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
        OcrResponse ocrResponse = new OcrResponse(contractId, "success");

        given(documentProcessService.processOcr(anyString(), any(OcrRequest.class)))
                .willReturn(ocrResponse);

        // when & then
        mockMvc.perform(multipart("/contract/ocr")
                .file(testFile1)
                .file(testFile2)
                .param("contractType", "employment")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.contractId").value(contractId))
                .andExpect(jsonPath("$.data.ocrStatus").value("success"));

        verify(documentProcessService).processOcr(anyString(), any(OcrRequest.class));
    }

    @Test
    @DisplayName("OCR 결과 조회 API 성공")
    void getOcrResults_Success() throws Exception {
        // given
        List<OcrContentResponse.HtmlElement> htmlArray = List.of(
            new OcrContentResponse.HtmlElement("content", "<div>테스트 내용 1</div>", 1001),
            new OcrContentResponse.HtmlElement("content", "<div>테스트 내용 2</div>", 1002)
        );
        OcrContentResponse response = new OcrContentResponse(2, "<div>테스트 내용 1</div><div>테스트 내용 2</div>", htmlArray);

        given(documentProcessService.getOcrResults(contractId))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/contract/ocr/{contractId}", contractId)
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pageIdx").value(2))
                .andExpect(jsonPath("$.data.htmlArray").isArray());

        verify(documentProcessService).getOcrResults(contractId);
    }

    @Test
    @DisplayName("OCR 내용 수정 API 성공")
    void updateOcrContent_Success() throws Exception {
        // given
        OcrUpdateRequest request = new OcrUpdateRequest("OCR001", "<div>수정된 내용</div>");

        // when & then
        mockMvc.perform(patch("/contract/ocr/{contractId}", contractId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(documentProcessService).updateOcrContent(contractId, request);
    }

    @Test
    @DisplayName("분석 요청 API 성공")
    void requestAnalysis_Success() throws Exception {
        // given
        AnalysisRequest request = new AnalysisRequest(contractId, true);
        AnalysisResponse response = new AnalysisResponse("ANALYSIS001");

        given(documentProcessService.requestAnalysis(request))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/contract/analysis")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.analysisId").value("ANALYSIS001"));

        verify(documentProcessService).requestAnalysis(request);
    }

    @Test
    @DisplayName("분석 결과 조회 API 성공")
    void getAnalysisResult_Success() throws Exception {
        // given
        AnalysisResultResponse response = new AnalysisResultResponse();
        response.setOriginContent("원본 계약서 내용");
        response.setSummary("계약서 요약");
        response.setAnalysisStatus("success");
        response.setAnalysisDate(LocalDateTime.now());
        response.setToxicCount(2);

        given(documentProcessService.getAnalysisResult(contractId, "ANALYSIS001"))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/contract/{contractId}/analysis/{analysisId}", contractId, "ANALYSIS001")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.originContent").value("원본 계약서 내용"))
                .andExpect(jsonPath("$.data.summary").value("계약서 요약"))
                .andExpect(jsonPath("$.data.toxicCount").value(2));

        verify(documentProcessService).getAnalysisResult(contractId, "ANALYSIS001");
    }
} 