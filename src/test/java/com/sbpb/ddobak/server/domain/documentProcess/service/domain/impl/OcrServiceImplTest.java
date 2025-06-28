package com.sbpb.ddobak.server.domain.documentProcess.service.domain.impl;

import com.sbpb.ddobak.server.domain.documentProcess.dto.OcrResponse;
import com.sbpb.ddobak.server.domain.documentProcess.entity.Contract;
import com.sbpb.ddobak.server.domain.documentProcess.entity.OcrContent;
import com.sbpb.ddobak.server.domain.documentProcess.exception.ContractExceptions;
import com.sbpb.ddobak.server.domain.documentProcess.repository.ContractRepository;
import com.sbpb.ddobak.server.domain.documentProcess.repository.OcrContentRepository;
import com.sbpb.ddobak.server.domain.documentProcess.service.infrastructure.ExternalOcrService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

/**
 * OcrServiceImpl 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OCR 서비스 테스트")
class OcrServiceImplTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private OcrContentRepository ocrContentRepository;

    @Mock
    private ExternalOcrService externalOcrService;

    @InjectMocks
    private OcrServiceImpl ocrService;

    private String userId;
    private List<MultipartFile> testFiles;
    private String contractType;

    @BeforeEach
    void setUp() {
        userId = "UTEST001";
        contractType = "employment";
        
        // Mock 파일 생성
        testFiles = List.of(
            new MockMultipartFile("file1", "contract1.jpg", "image/jpeg", "test content 1".getBytes()),
            new MockMultipartFile("file2", "contract2.jpg", "image/jpeg", "test content 2".getBytes())
        );
    }

    @Test
    @DisplayName("다중 파일 OCR 처리 성공")
    void processMultipleFiles_Success() {
        // given
        String contractId = "CONTRACT001";
        Contract savedContract = Contract.builder()
                .id(contractId)
                .userId(userId)
                .imgS3Key("contracts/" + contractId + "/")
                .build();

        OcrContent ocrContent1 = OcrContent.builder()
                .id("OCR001")
                .contractId(contractId)
                .content("<html>OCR Result 1</html>")
                .tagIdx(0)
                .build();

        OcrContent ocrContent2 = OcrContent.builder()
                .id("OCR002")
                .contractId(contractId)
                .content("<html>OCR Result 2</html>")
                .tagIdx(1)
                .build();

        given(contractRepository.save(any(Contract.class))).willReturn(savedContract);
        given(ocrContentRepository.save(any(OcrContent.class)))
                .willReturn(ocrContent1)
                .willReturn(ocrContent2);
        given(externalOcrService.processOcrForFile(any(), anyString(), any()))
                .willReturn("<html>OCR Result 1</html>")
                .willReturn("<html>OCR Result 2</html>");

        // when
        OcrResponse result = ocrService.processMultipleFiles(userId, testFiles, contractType);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContractId()).isEqualTo(contractId);
        assertThat(result.getOcrResults()).hasSize(2);
        assertThat(result.getOcrResults().get(0).getPageIndex()).isEqualTo(0);
        assertThat(result.getOcrResults().get(1).getPageIndex()).isEqualTo(1);

        verify(contractRepository).save(any(Contract.class));
        verify(ocrContentRepository, times(2)).save(any(OcrContent.class)); // 2개 파일이므로 2번 호출
        verify(externalOcrService, times(2)).processOcrForFile(any(), anyString(), any()); // 2개 파일이므로 2번 호출
    }

    @Test
    @DisplayName("OCR 컨텐츠 업데이트 성공")
    void updateOcrContent_Success() {
        // given
        String contractId = "CONTRACT001";
        Integer pageIndex = 0;
        String newContent = "<html>Updated Content</html>";

        OcrContent existingContent = OcrContent.builder()
                .id("OCR001")
                .contractId(contractId)
                .content("<html>Original Content</html>")
                .tagIdx(pageIndex)
                .build();

        given(contractRepository.existsById(contractId)).willReturn(true);
        given(ocrContentRepository.findByContractIdAndTagIdx(contractId, pageIndex))
                .willReturn(Optional.of(existingContent));
        given(ocrContentRepository.save(any(OcrContent.class))).willReturn(existingContent);

        // when
        ocrService.updateOcrContent(contractId, pageIndex, newContent);

        // then
        verify(contractRepository).existsById(contractId);
        verify(ocrContentRepository).findByContractIdAndTagIdx(contractId, pageIndex);
        verify(ocrContentRepository).save(any(OcrContent.class));
    }

    @Test
    @DisplayName("존재하지 않는 계약서로 OCR 컨텐츠 업데이트 시 예외 발생")
    void updateOcrContent_ContractNotFound_ThrowsException() {
        // given
        String contractId = "NONEXISTENT";
        Integer pageIndex = 0;
        String newContent = "<html>Updated Content</html>";

        given(contractRepository.existsById(contractId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> ocrService.updateOcrContent(contractId, pageIndex, newContent))
                .isInstanceOf(ContractExceptions.ContractNotFoundException.class);

        verify(contractRepository).existsById(contractId);
    }

    @Test
    @DisplayName("OCR 결과 조회 성공")
    void getOcrResults_Success() {
        // given
        String contractId = "CONTRACT001";
        List<OcrContent> ocrContents = List.of(
            OcrContent.builder()
                    .id("OCR001")
                    .contractId(contractId)
                    .content("<html>Content 1</html>")
                    .tagIdx(0)
                    .build(),
            OcrContent.builder()
                    .id("OCR002")
                    .contractId(contractId)
                    .content("<html>Content 2</html>")
                    .tagIdx(1)
                    .build()
        );

        given(contractRepository.existsById(contractId)).willReturn(true);
        given(ocrContentRepository.findByContractIdOrderByTagIdx(contractId)).willReturn(ocrContents);

        // when
        List<OcrResponse.OcrResult> result = ocrService.getOcrResults(contractId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getOcrContentId()).isEqualTo("OCR001");
        assertThat(result.get(0).getPageIndex()).isEqualTo(0);
        assertThat(result.get(1).getOcrContentId()).isEqualTo("OCR002");
        assertThat(result.get(1).getPageIndex()).isEqualTo(1);

        verify(contractRepository).existsById(contractId);
        verify(ocrContentRepository).findByContractIdOrderByTagIdx(contractId);
    }

    @Test
    @DisplayName("존재하지 않는 계약서로 OCR 결과 조회 시 예외 발생")
    void getOcrResults_ContractNotFound_ThrowsException() {
        // given
        String contractId = "NONEXISTENT";

        given(contractRepository.existsById(contractId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> ocrService.getOcrResults(contractId))
                .isInstanceOf(ContractExceptions.ContractNotFoundException.class);

        verify(contractRepository).existsById(contractId);
    }
} 