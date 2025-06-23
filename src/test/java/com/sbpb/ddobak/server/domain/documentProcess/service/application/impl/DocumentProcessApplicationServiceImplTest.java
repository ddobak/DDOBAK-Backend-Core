package com.sbpb.ddobak.server.domain.documentProcess.service.application.impl;

import com.sbpb.ddobak.server.domain.documentProcess.dto.*;
import com.sbpb.ddobak.server.domain.documentProcess.entity.Contract;
import com.sbpb.ddobak.server.domain.documentProcess.entity.ContractAnalysis;
import com.sbpb.ddobak.server.domain.documentProcess.repository.ContractAnalysisRepository;
import com.sbpb.ddobak.server.domain.documentProcess.repository.ContractRepository;
import com.sbpb.ddobak.server.domain.documentProcess.repository.OcrContentRepository;
import com.sbpb.ddobak.server.domain.documentProcess.service.domain.AnalysisService;
import com.sbpb.ddobak.server.domain.documentProcess.service.domain.OcrService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * DocumentProcessApplicationServiceImpl 통합 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("문서 처리 Application Service 테스트")
class DocumentProcessApplicationServiceImplTest {

    @Mock
    private OcrService ocrService;

    @Mock
    private AnalysisService analysisService;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private OcrContentRepository ocrContentRepository;

    @Mock
    private ContractAnalysisRepository contractAnalysisRepository;

    @InjectMocks
    private DocumentProcessApplicationServiceImpl documentProcessApplicationService;

    private String userId;
    private List<MultipartFile> testFiles;
    private String contractType;

    @BeforeEach
    void setUp() {
        userId = "UTEST001";
        contractType = "employment";
        
        testFiles = List.of(
            new MockMultipartFile("file1", "contract1.jpg", "image/jpeg", "test content 1".getBytes()),
            new MockMultipartFile("file2", "contract2.jpg", "image/jpeg", "test content 2".getBytes())
        );
    }

    @Test
    @DisplayName("전체 문서 처리 플로우 성공 (OCR + 자동 분석 요청)")
    void processDocumentComplete_Success() {
        // given
        String contractId = "CONTRACT001";
        
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

        AnalysisResponse analysisResponse = AnalysisResponse.builder()
                .contractId(contractId)
                .analysisId("ANALYSIS001")
                .status("REQUESTED")
                .message("Analysis request submitted successfully")
                .build();

        given(ocrService.processMultipleFiles(userId, testFiles, contractType)).willReturn(ocrResponse);
        given(analysisService.requestAnalysis(contractId)).willReturn(analysisResponse);

        // when
        DocumentProcessResponse result = documentProcessApplicationService
                .processDocumentComplete(userId, testFiles, contractType);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContractId()).isEqualTo(contractId);
        assertThat(result.getOcrResults()).hasSize(2);
        assertThat(result.getAnalysisInfo()).isNotNull();
        assertThat(result.getAnalysisInfo().getStatus()).isEqualTo("REQUESTED");
        assertThat(result.getProcessingStatus()).isEqualTo("OCR_COMPLETED_ANALYSIS_REQUESTED");

        verify(ocrService).processMultipleFiles(userId, testFiles, contractType);
        verify(analysisService).requestAnalysis(contractId);
    }

    @Test
    @DisplayName("OCR만 처리 성공")
    void processOcrOnly_Success() {
        // given
        String contractId = "CONTRACT001";
        
        OcrResponse ocrResponse = OcrResponse.builder()
                .contractId(contractId)
                .ocrResults(List.of(
                    OcrResponse.OcrResult.builder()
                            .ocrContentId("OCR001")
                            .pageIndex(0)
                            .htmlContent("<html>Content 1</html>")
                            .build()
                ))
                .build();

        given(ocrService.processMultipleFiles(userId, testFiles, contractType)).willReturn(ocrResponse);

        // when
        OcrResponse result = documentProcessApplicationService.processOcrOnly(userId, testFiles, contractType);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContractId()).isEqualTo(contractId);
        assertThat(result.getOcrResults()).hasSize(1);

        verify(ocrService).processMultipleFiles(userId, testFiles, contractType);
    }

    @Test
    @DisplayName("OCR 컨텐츠 업데이트 성공")
    void updateOcrContent_Success() {
        // given
        OcrContentUpdateRequest request = OcrContentUpdateRequest.builder()
                .contractId("CONTRACT001")
                .pageIndex(0)
                .content("<html>Updated Content</html>")
                .build();

        // when
        documentProcessApplicationService.updateOcrContent(request);

        // then
        verify(ocrService).updateOcrContent(request.getContractId(), request.getPageIndex(), request.getContent());
    }

    @Test
    @DisplayName("분석 요청 성공")
    void requestAnalysis_Success() {
        // given
        String contractId = "CONTRACT001";
        AnalysisRequest request = AnalysisRequest.builder()
                .contractId(contractId)
                .build();

        AnalysisResponse expectedResponse = AnalysisResponse.builder()
                .contractId(contractId)
                .analysisId("ANALYSIS001")
                .status("REQUESTED")
                .message("Analysis request submitted successfully")
                .build();

        given(analysisService.requestAnalysis(contractId)).willReturn(expectedResponse);

        // when
        AnalysisResponse result = documentProcessApplicationService.requestAnalysis(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContractId()).isEqualTo(contractId);
        assertThat(result.getStatus()).isEqualTo("REQUESTED");

        verify(analysisService).requestAnalysis(contractId);
    }

    @Test
    @DisplayName("처리 상태 조회 성공")
    void getProcessStatus_Success() {
        // given
        String contractId = "CONTRACT001";
        LocalDateTime now = LocalDateTime.now();

        Contract contract = Contract.builder()
                .id(contractId)
                .userId(userId)
                .imgS3Key("contracts/" + contractId + "/")
                .createdAt(now)
                .updatedAt(now)
                .build();

        ContractAnalysis analysis = ContractAnalysis.builder()
                .id("ANALYSIS001")
                .contractId(contractId)
                .summary("Analysis completed")
                .createdAt(now)
                .updatedAt(now)
                .build();

        given(contractRepository.findById(contractId)).willReturn(Optional.of(contract));
        given(ocrContentRepository.countByContractId(contractId)).willReturn(2L);
        given(analysisService.getAnalysisStatus(contractId)).willReturn("COMPLETED");
        given(contractAnalysisRepository.findByContractId(contractId)).willReturn(Optional.of(analysis));

        // when
        DocumentProcessStatusResponse result = documentProcessApplicationService.getProcessStatus(contractId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContractId()).isEqualTo(contractId);
        assertThat(result.getOcrStatus()).isEqualTo("COMPLETED");
        assertThat(result.getAnalysisStatus()).isEqualTo("COMPLETED");
        assertThat(result.getOverallStatus()).isEqualTo("ALL_COMPLETED");
        assertThat(result.getTotalPages()).isEqualTo(2);

        verify(contractRepository).findById(contractId);
        verify(ocrContentRepository).countByContractId(contractId);
        verify(analysisService).getAnalysisStatus(contractId);
        verify(contractAnalysisRepository).findByContractId(contractId);
    }

    @Test
    @DisplayName("OCR 결과 조회 성공")
    void getOcrResults_Success() {
        // given
        String contractId = "CONTRACT001";
        
        List<OcrResponse.OcrResult> expectedResults = List.of(
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

        given(ocrService.getOcrResults(contractId)).willReturn(expectedResults);

        // when
        List<OcrResponse.OcrResult> result = documentProcessApplicationService.getOcrResults(contractId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getOcrContentId()).isEqualTo("OCR001");
        assertThat(result.get(1).getOcrContentId()).isEqualTo("OCR002");

        verify(ocrService).getOcrResults(contractId);
    }
} 