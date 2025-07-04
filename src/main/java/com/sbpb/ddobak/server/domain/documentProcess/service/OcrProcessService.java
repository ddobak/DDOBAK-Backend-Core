package com.sbpb.ddobak.server.domain.documentProcess.service;

import com.sbpb.ddobak.server.common.utils.IdGenerator;
import com.sbpb.ddobak.server.common.utils.LambdaUtil;
import com.sbpb.ddobak.server.common.utils.S3Util;
import com.sbpb.ddobak.server.config.AwsConfig;
import com.sbpb.ddobak.server.domain.documentProcess.dto.lambda.OcrLambdaPayload;
import com.sbpb.ddobak.server.domain.documentProcess.dto.lambda.OcrLambdaResponse;
import com.sbpb.ddobak.server.domain.documentProcess.dto.ocr.*;
import com.sbpb.ddobak.server.domain.documentProcess.entity.Contract;
import com.sbpb.ddobak.server.domain.documentProcess.entity.OcrContent;
import com.sbpb.ddobak.server.domain.documentProcess.repository.ContractRepository;
import com.sbpb.ddobak.server.domain.documentProcess.repository.OcrContentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * OCR 처리 전담 서비스
 */
@Service
@Transactional
public class OcrProcessService {

    private static final Logger log = LoggerFactory.getLogger(OcrProcessService.class);

    private final ContractRepository contractRepository;
    private final OcrContentRepository ocrContentRepository;
    private final S3Util s3Util;
    private final LambdaUtil lambdaUtil;
    private final AwsConfig awsConfig;
    
    // OCR 병렬 처리용 스레드 풀 (최대 10개 동시 처리)
    private final Executor ocrExecutor = Executors.newFixedThreadPool(10);

    public OcrProcessService(ContractRepository contractRepository,
                             OcrContentRepository ocrContentRepository,
                             S3Util s3Util,
                             LambdaUtil lambdaUtil,
                             AwsConfig awsConfig) {
        this.contractRepository = contractRepository;
        this.ocrContentRepository = ocrContentRepository;
        this.s3Util = s3Util;
        this.lambdaUtil = lambdaUtil;
        this.awsConfig = awsConfig;
    }

    /**
     * OCR 처리 요청 - 병렬 처리
     */
    public OcrResponse processOcr(String userId, OcrRequest request) {
        // 1. 계약 생성
        String contractId = IdGenerator.generate();
        String s3KeyPrefix = "contracts/" + contractId + "/";
        
        Contract contract = new Contract(contractId, userId, s3KeyPrefix);
        contractRepository.save(contract);

        try {
            List<MultipartFile> files = request.getFiles();
            
            // 2. 모든 파일을 병렬로 S3 업로드 및 Lambda 호출
            List<CompletableFuture<OcrPageResult>> futures = IntStream.range(0, files.size())
                .mapToObj(pageIdx -> processOcrPageAsync(files.get(pageIdx), contractId, s3KeyPrefix, pageIdx))
                .toList();
            
            log.info("OCR 병렬 처리 시작 - ContractId: {}, Files: {}", contractId, files.size());
            
            // 3. 모든 작업 완료 대기
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            
            allFutures.join(); // 모든 작업 완료까지 대기
            
            // 4. 결과를 페이지 순서대로 정렬하여 저장
            List<OcrPageResult> results = futures.stream()
                .map(CompletableFuture::join)
                .filter(result -> result != null && result.isSuccess())
                .sorted((r1, r2) -> Integer.compare(r1.getPageIdx(), r2.getPageIdx()))
                .toList();
            
            // 5. 정렬된 순서로 DB 저장
            for (OcrPageResult result : results) {
                saveOcrContents(contractId, result);
            }
            
            log.info("OCR 병렬 처리 완료 - ContractId: {}, Success: {}/{}", 
                    contractId, results.size(), files.size());
            
            return new OcrResponse(contractId, "success");
            
        } catch (Exception e) {
            log.error("OCR 처리 실패 - ContractId: {}", contractId, e);
            return new OcrResponse(contractId, "fail");
        }
    }
    
    /**
     * 단일 페이지 OCR 처리 (비동기)
     */
    private CompletableFuture<OcrPageResult> processOcrPageAsync(MultipartFile file, String contractId, 
                                                                String s3KeyPrefix, int pageIdx) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String s3Key = s3KeyPrefix + pageIdx + "_" + file.getOriginalFilename();
                
                // S3 업로드
                s3Util.upload(awsConfig.getS3().getServiceBucket(), s3Key, file);
                
                // Lambda 동기 호출
                OcrLambdaPayload payload = new OcrLambdaPayload(s3Key, pageIdx);
                
                log.debug("OCR Lambda 호출 준비 - ContractId: {}, S3Key: {}, PageIdx: {}", 
                         contractId, s3Key, pageIdx);
                
                InvokeResponse response = lambdaUtil.invokeAndWait(
                    awsConfig.getLambda().getOcrFunctionName(), 
                    payload);
                
                log.debug("OCR Lambda 호출 완료 - ContractId: {}, S3Key: {}, StatusCode: {}", 
                         contractId, s3Key, response.statusCode());
                
                // Lambda 응답 처리
                if (lambdaUtil.hasError(response)) {
                    log.error("OCR Lambda 오류 - ContractId: {}, S3Key: {}, Response: {}", 
                             contractId, s3Key, response.payload().asUtf8String());
                    return null;
                }
                
                // OCR 결과 파싱
                OcrLambdaResponse ocrResponse = lambdaUtil.parseResponse(response, OcrLambdaResponse.class);
                
                if (ocrResponse.isSuccess() && ocrResponse.getData() != null) {
                    return new OcrPageResult(pageIdx, ocrResponse.getData().getHtmlArray(), true);
                } else {
                    log.warn("OCR Lambda 처리 실패 - ContractId: {}, PageIdx: {}, Message: {}", 
                            contractId, pageIdx, ocrResponse.getMessage());
                    return null;
                }
                
            } catch (Exception e) {
                log.error("OCR 페이지 처리 실패 - ContractId: {}, PageIdx: {}", contractId, pageIdx, e);
                return null;
            }
        }, ocrExecutor);
    }
    
    /**
     * OCR 결과를 DB에 저장
     */
    private void saveOcrContents(String contractId, OcrPageResult result) {
        List<OcrLambdaResponse.HtmlElement> htmlElements = result.getHtmlElements();
        int pageIdx = result.getPageIdx();
        
        for (int j = 0; j < htmlElements.size(); j++) {
            OcrLambdaResponse.HtmlElement element = htmlElements.get(j);
            
            OcrContent ocrContent = new OcrContent(
                IdGenerator.generate(),
                contractId,
                element.getHtml(),
                (pageIdx+1) * 1000 + j // tagIdx: 페이지번호*1000 + 요소순서로 정렬
            );
            
            ocrContentRepository.save(ocrContent);
        }
        
        log.info("OCR 결과 저장 완료 - ContractId: {}, PageIdx: {}, Elements: {}", 
                contractId, pageIdx, htmlElements.size());
    }

    /**
     * OCR 결과 조회
     */
    @Transactional(readOnly = true)
    public OcrContentResponse getOcrResults(String contractId) {
        List<OcrContent> ocrContents = ocrContentRepository.findByContractIdOrderByTagIdx(contractId);
        
        // HTML 전체 내용 조합
        StringBuilder htmlEntire = new StringBuilder();
        List<OcrContentResponse.HtmlElement> htmlArray = new ArrayList<>();
        
        for (OcrContent content : ocrContents) {
            htmlEntire.append(content.getContent());
            htmlArray.add(new OcrContentResponse.HtmlElement(
                "content", 
                content.getContent(), 
                content.getId(),    // OCR 콘텐츠의 ID 추가
                content.getTagIdx()
            ));
        }
        
        return new OcrContentResponse(
            ocrContents.size(),
            htmlEntire.toString(),
            htmlArray
        );
    }

    /**
     * OCR 내용 수정
     */
    public void updateOcrContent(String contractId, OcrUpdateRequest request) {
        OcrContent ocrContent = ocrContentRepository.findById(request.getId())
            .orElseThrow(() -> new RuntimeException("OCR content not found"));
        
        if (!ocrContent.getContractId().equals(contractId)) {
            throw new RuntimeException("Contract ID mismatch");
        }
        
        ocrContent.setContent(request.getElement());
        ocrContentRepository.save(ocrContent);
    }
    
    /**
     * OCR 페이지 처리 결과를 담는 내부 클래스
     */
    private static class OcrPageResult {
        private final int pageIdx;
        private final List<OcrLambdaResponse.HtmlElement> htmlElements;
        private final boolean success;
        
        public OcrPageResult(int pageIdx, List<OcrLambdaResponse.HtmlElement> htmlElements, boolean success) {
            this.pageIdx = pageIdx;
            this.htmlElements = htmlElements;
            this.success = success;
        }
        
        public int getPageIdx() { return pageIdx; }
        public List<OcrLambdaResponse.HtmlElement> getHtmlElements() { return htmlElements; }
        public boolean isSuccess() { return success; }
    }
} 