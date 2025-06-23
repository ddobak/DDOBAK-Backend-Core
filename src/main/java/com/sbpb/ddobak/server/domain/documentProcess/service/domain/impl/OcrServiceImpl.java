package com.sbpb.ddobak.server.domain.documentProcess.service.domain.impl;

import com.sbpb.ddobak.server.common.utils.IdGenerator;
import com.sbpb.ddobak.server.domain.documentProcess.dto.OcrResponse;
import com.sbpb.ddobak.server.domain.documentProcess.entity.Contract;
import com.sbpb.ddobak.server.domain.documentProcess.entity.OcrContent;
import com.sbpb.ddobak.server.domain.documentProcess.exception.ContractExceptions;
import com.sbpb.ddobak.server.domain.documentProcess.repository.ContractRepository;
import com.sbpb.ddobak.server.domain.documentProcess.repository.OcrContentRepository;
import com.sbpb.ddobak.server.domain.documentProcess.service.domain.OcrService;
import com.sbpb.ddobak.server.domain.documentProcess.service.infrastructure.ExternalOcrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

/**
 * OCR 처리 도메인 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OcrServiceImpl implements OcrService {

    private final ContractRepository contractRepository;
    private final OcrContentRepository ocrContentRepository;
    private final ExternalOcrService externalOcrService;

    @Override
    @Transactional
    public OcrResponse processMultipleFiles(String userId, List<MultipartFile> files, String contractType) {
        log.info("Starting OCR processing for {} files, userId: {}", files.size(), userId);

        // 1. 계약서 생성
        Contract contract = createContract(userId);

        // 2. 파일들을 S3에 업로드하고 병렬로 OCR 처리
        List<CompletableFuture<OcrResponse.OcrResult>> futures = IntStream.range(0, files.size())
                .mapToObj(index -> CompletableFuture.supplyAsync(() -> {
                    try {
                        MultipartFile file = files.get(index);
                        
                        // S3 업로드 및 OCR 처리
                        String htmlContent = externalOcrService.processOcrForFile(file, contract.getId(), index);
                        
                        // OCR 결과 저장
                        OcrContent ocrContent = saveOcrContent(contract.getId(), htmlContent, index);
                        
                        return OcrResponse.OcrResult.builder()
                                .ocrContentId(ocrContent.getId())
                                .pageIndex(index)
                                .htmlContent(htmlContent)
                                .build();
                    } catch (Exception e) {
                        log.error("Failed to process OCR for file index {}: {}", index, e.getMessage(), e);
                        throw new RuntimeException("OCR processing failed for file " + index, e);
                    }
                }))
                .toList();

        // 3. 모든 비동기 작업 완료 대기
        List<OcrResponse.OcrResult> ocrResults = futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("Failed to get OCR result: {}", e.getMessage(), e);
                        throw new RuntimeException("Failed to complete OCR processing", e);
                    }
                })
                .toList();

        log.info("OCR processing completed for contract: {}, total pages: {}", contract.getId(), ocrResults.size());

        return OcrResponse.builder()
                .contractId(contract.getId())
                .ocrResults(ocrResults)
                .build();
    }

    @Override
    @Transactional
    public void updateOcrContent(String contractId, Integer pageIndex, String content) {
        log.info("Updating OCR content for contract: {}, pageIndex: {}", contractId, pageIndex);

        // 계약서 존재 여부 확인
        if (!contractRepository.existsById(contractId)) {
            throw ContractExceptions.contractNotFound(contractId);
        }

        // OCR 컨텐츠 조회 및 업데이트
        OcrContent ocrContent = ocrContentRepository.findByContractIdAndTagIdx(contractId, pageIndex)
                .orElseThrow(() -> ContractExceptions.ocrContentNotFound(contractId, pageIndex));

        ocrContent.updateContent(content);
        ocrContentRepository.save(ocrContent);

        log.info("OCR content updated successfully for contract: {}, pageIndex: {}", contractId, pageIndex);
    }

    @Override
    public List<OcrResponse.OcrResult> getOcrResults(String contractId) {
        log.info("Retrieving OCR results for contract: {}", contractId);

        // 계약서 존재 여부 확인
        if (!contractRepository.existsById(contractId)) {
            throw ContractExceptions.contractNotFound(contractId);
        }

        List<OcrContent> ocrContents = ocrContentRepository.findByContractIdOrderByTagIdx(contractId);

        return ocrContents.stream()
                .map(content -> OcrResponse.OcrResult.builder()
                        .ocrContentId(content.getId())
                        .pageIndex(content.getTagIdx())
                        .htmlContent(content.getContent())
                        .build())
                .toList();
    }

    /**
     * 계약서 생성
     */
    private Contract createContract(String userId) {
        String contractId = IdGenerator.generate();
        String s3Key = String.format("contracts/%s/", contractId);

        Contract contract = Contract.builder()
                .id(contractId)
                .userId(userId)
                .imgS3Key(s3Key)
                .build();

        return contractRepository.save(contract);
    }

    /**
     * OCR 컨텐츠 저장
     */
    private OcrContent saveOcrContent(String contractId, String htmlContent, Integer tagIdx) {
        OcrContent ocrContent = OcrContent.builder()
                .id(IdGenerator.generate())
                .contractId(contractId)
                .content(htmlContent)
                .tagIdx(tagIdx)
                .build();

        return ocrContentRepository.save(ocrContent);
    }
} 