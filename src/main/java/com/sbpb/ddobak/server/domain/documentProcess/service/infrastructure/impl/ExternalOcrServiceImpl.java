package com.sbpb.ddobak.server.domain.documentProcess.service.infrastructure.impl;

import com.sbpb.ddobak.server.common.response.ApiResponse;
import com.sbpb.ddobak.server.domain.documentProcess.service.infrastructure.ExternalOcrService;
import com.sbpb.ddobak.server.infrastructure.aws.lambda.LambdaInvoker;
import com.sbpb.ddobak.server.infrastructure.aws.s3.S3ClientAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * 외부 OCR 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalOcrServiceImpl implements ExternalOcrService {

    private final S3ClientAdapter s3ClientAdapter;
    private final LambdaInvoker lambdaInvoker;
    
    @Value("${aws.s3.service-bucket}")
    private String serviceBucket;

    @Override
    public String processOcrForFile(MultipartFile file, String contractId, Integer pageIndex) {
        try {
            log.info("Processing OCR for contract: {}, page: {}", contractId, pageIndex);

            // 1. S3에 파일 업로드
            String s3Key = String.format("contracts/%s/page_%d_%s", contractId, pageIndex, file.getOriginalFilename());
            boolean uploadSuccess = s3ClientAdapter.uploadObject(serviceBucket, s3Key, file.getInputStream(), file.getSize());
            
            if (!uploadSuccess) {
                throw new RuntimeException("Failed to upload file to S3");
            }
            
            log.info("File uploaded to S3: {}", s3Key);

            // 2. Lambda OCR 함수 호출
            Map<String, Object> ocrRequest = createOcrLambdaRequest(s3Key, contractId, pageIndex);
            ApiResponse<Map<String, Object>> ocrResponse = lambdaInvoker.invoke("ocr-function", ocrRequest);
            
            // 3. OCR 결과에서 HTML 컨텐츠 추출
            String htmlContent = extractHtmlFromResponse(ocrResponse);
            
            log.info("OCR processing completed for contract: {}, page: {}", contractId, pageIndex);
            
            return htmlContent;
            
        } catch (Exception e) {
            log.error("Failed to process OCR for contract: {}, page: {}: {}", contractId, pageIndex, e.getMessage(), e);
            throw new RuntimeException("OCR processing failed", e);
        }
    }

    /**
     * OCR Lambda 요청 Map 생성
     */
    private Map<String, Object> createOcrLambdaRequest(String s3Key, String contractId, Integer pageIndex) {
        return Map.of(
            "s3Key", s3Key,
            "contractId", contractId,
            "pageIndex", pageIndex,
            "bucketName", serviceBucket
        );
    }

    /**
     * OCR 응답에서 HTML 컨텐츠 추출
     */
    private String extractHtmlFromResponse(ApiResponse<Map<String, Object>> ocrResponse) {
        // TODO: 실제 OCR Lambda 응답 형식에 맞춰 파싱 로직 구현
        // 현재는 임시로 응답을 그대로 반환
        log.debug("OCR response: {}", ocrResponse);
        
        if (ocrResponse.isSuccess() && ocrResponse.getData() != null) {
            Map<String, Object> data = ocrResponse.getData();
            return data.getOrDefault("htmlContent", "").toString();
        }
        
        return "";
    }
} 