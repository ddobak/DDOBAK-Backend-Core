package com.sbpb.ddobak.server.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class LambdaUtil {

    private static final Logger log = LoggerFactory.getLogger(LambdaUtil.class);

    private final LambdaClient lambdaClient;
    private final ObjectMapper objectMapper;

    public LambdaUtil(LambdaClient lambdaClient, ObjectMapper objectMapper) {
        this.lambdaClient = lambdaClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Lambda 함수 목록 조회
     */
    public List<FunctionConfiguration> listFunctions() {
        ListFunctionsRequest request = ListFunctionsRequest.builder().build();
        ListFunctionsResponse response = lambdaClient.listFunctions(request);
        return response.functions();
    }

    /**
     * Lambda 함수 비동기 호출
     */
    public CompletableFuture<InvokeResponse> invokeAsync(String lambdaName, Object payload) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String jsonPayload = objectMapper.writeValueAsString(payload);
                
                log.debug("Lambda 비동기 호출 시작 - Function: {}, Payload: {}", lambdaName, jsonPayload);
                
                InvokeRequest invokeRequest = InvokeRequest.builder()
                        .functionName(lambdaName)
                        .payload(SdkBytes.fromUtf8String(jsonPayload))
                        .invocationType(InvocationType.EVENT) // 비동기 호출
                        .build();

                InvokeResponse response = lambdaClient.invoke(invokeRequest);
                
                log.debug("Lambda 비동기 호출 완료 - Function: {}, StatusCode: {}", 
                         lambdaName, response.statusCode());
                
                return response;
            } catch (Exception e) {
                log.error("Lambda 비동기 호출 실패 - Function: {}, Error: {}", lambdaName, e.getMessage(), e);
                throw new RuntimeException("Failed to invoke lambda function asynchronously", e);
            }
        });
    }

    /**
     * Lambda 함수 동기 호출 (응답 대기)
     */
    public InvokeResponse invokeAndWait(String lambdaName, Object payload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            
            log.debug("Lambda 동기 호출 시작 - Function: {}, Payload: {}", lambdaName, jsonPayload);
            
            InvokeRequest invokeRequest = InvokeRequest.builder()
                    .functionName(lambdaName)
                    .payload(SdkBytes.fromUtf8String(jsonPayload))
                    .invocationType(InvocationType.REQUEST_RESPONSE) // 동기 호출
                    .build();

            InvokeResponse response = lambdaClient.invoke(invokeRequest);
            
            String responsePayload = response.payload().asUtf8String();
            log.debug("Lambda 동기 호출 완료 - Function: {}, StatusCode: {}, Response: {}", 
                     lambdaName, response.statusCode(), responsePayload);
            
            return response;
        } catch (Exception e) {
            log.error("Lambda 동기 호출 실패 - Function: {}, Error: {}", lambdaName, e.getMessage(), e);
            throw new RuntimeException("Failed to invoke lambda function synchronously", e);
        }
    }

    /**
     * Lambda 함수 응답을 특정 타입으로 변환
     */
    public <T> T parseResponse(InvokeResponse response, Class<T> responseType) {
        try {
            String responsePayload = response.payload().asUtf8String();
            return objectMapper.readValue(responsePayload, responseType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse lambda response", e);
        }
    }

    /**
     * Lambda 함수 오류 확인
     */
    public boolean hasError(InvokeResponse response) {
        return response.functionError() != null || response.statusCode() != 200;
    }

    /**
     * Lambda 함수 로그 결과 조회
     */
    public String getLogResult(InvokeResponse response) {
        return response.logResult();
    }
} 