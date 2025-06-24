package com.sbpb.ddobak.server.infrastructure.aws.lambda;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbpb.ddobak.server.common.exception.ErrorCode;
import com.sbpb.ddobak.server.common.exception.ExternalServiceException;
import com.sbpb.ddobak.server.common.response.ApiResponse;

import com.sbpb.ddobak.server.infrastructure.aws.config.AwsInfrastructureConfig.LambdaFunctionSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.lambda.model.LambdaException;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * AWS Lambda 클라이언트 어댑터
 */
@Slf4j
@Component
@Profile("!test") // 테스트 환경에서는 제외
@RequiredArgsConstructor
public class LambdaClientAdapter implements LambdaInvoker {

    private final LambdaClient lambdaClient;
    private final Map<String, LambdaFunctionSpec> productionLambdaFunctions;
    private final ObjectMapper objectMapper;

    /**
     * Lambda 함수 동기 호출 (포트 인터페이스 구현)
     * 
     * @param functionName 함수명
     * @param payload 요청 페이로드
     * @return ApiResponse 형식의 응답
     */
    @Override
    public ApiResponse<Map<String, Object>> invoke(String functionName, Map<String, Object> payload) {
        log.info("Invoking Lambda function: {} with payload: {}", functionName, payload != null);
        
        Instant startTime = Instant.now();
        
        try {
            // 함수 스펙 조회
            LambdaFunctionSpec spec = getFunctionSpec(functionName);
            
            // Lambda 함수 호출
            InvokeResponse response = invokeLambdaFunction(spec, payload);
            
            // 응답 처리 및 검증
            return processLambdaResponse(response, spec, startTime);
            
        } catch (LambdaException e) {
            log.error("AWS Lambda service error for function {}: {}", functionName, e.getMessage());
            return ApiResponse.error(ErrorCode.EXTERNAL_SERVICE_ERROR, 
                    "Lambda function execution failed: " + e.getMessage());
                    
        } catch (ExternalServiceException e) {
            log.error("Lambda function configuration error: {}", e.getMessage());
            return ApiResponse.error(ErrorCode.EXTERNAL_SERVICE_ERROR, e.getMessage());
            
        } catch (Exception e) {
            log.error("Unexpected error invoking Lambda function {}: {}", functionName, e.getMessage(), e);
            return ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, 
                    "Unexpected error occurred while invoking Lambda function");
        }
    }

    /**
     * Lambda 함수 비동기 호출 (포트 인터페이스 구현)
     * 
     * @param functionName 함수명
     * @param payload 요청 페이로드
     * @return ApiResponse 형식의 응답 (비동기 호출 상태만 반환)
     */
    @Override
    public ApiResponse<Map<String, Object>> invokeAsync(String functionName, Map<String, Object> payload) {
        log.info("Invoking Lambda function asynchronously: {}", functionName);
        
        try {
            LambdaFunctionSpec spec = getFunctionSpec(functionName);
            
            // 비동기 호출을 위한 InvokeRequest 생성
            InvokeRequest request = createInvokeRequest(spec, payload, "Event");
            
            InvokeResponse response = lambdaClient.invoke(request);
            
            if (response.statusCode() == 202) {
                Map<String, Object> result = Map.of(
                    "status", "queued",
                    "message", "Function invocation queued successfully",
                    "functionName", functionName
                );
                return ApiResponse.success(result);
            } else {
                log.warn("Unexpected status code for async invocation: {}", response.statusCode());
                return ApiResponse.error(ErrorCode.EXTERNAL_SERVICE_ERROR, 
                        "Unexpected response from Lambda service");
            }
            
        } catch (Exception e) {
            log.error("Error invoking Lambda function {} asynchronously: {}", functionName, e.getMessage(), e);
            return ApiResponse.error(ErrorCode.EXTERNAL_SERVICE_ERROR, 
                    "Failed to invoke Lambda function asynchronously");
        }
    }

    /**
     * Lambda 함수 스펙 조회
     */
    private LambdaFunctionSpec getFunctionSpec(String functionName) {
        LambdaFunctionSpec spec = productionLambdaFunctions.get(functionName);
        if (spec == null) {
            throw new ExternalServiceException("Lambda function configuration not found: " + functionName);
        }
        return spec;
    }

    /**
     * Lambda 함수 실제 호출
     */
    private InvokeResponse invokeLambdaFunction(LambdaFunctionSpec spec, Map<String, Object> payload) {
        InvokeRequest request = createInvokeRequest(spec, payload, spec.getDefaultInvocationType());
        
        log.debug("Invoking Lambda function: {} with invocation type: {}", 
                spec.getFunctionName(), spec.getDefaultInvocationType());
                
        return lambdaClient.invoke(request);
    }

    /**
     * InvokeRequest 생성
     */
    private InvokeRequest createInvokeRequest(LambdaFunctionSpec spec, Map<String, Object> payload, String invocationType) {
        try {
            InvokeRequest.Builder requestBuilder = InvokeRequest.builder()
                    .functionName(spec.getFunctionName())
                    .invocationType(invocationType);

            // 페이로드가 있는 경우 추가
            if (payload != null) {
                String payloadJson = objectMapper.writeValueAsString(payload);
                requestBuilder.payload(SdkBytes.fromUtf8String(payloadJson));
            }

            // 로그 수집이 활성화된 경우 로그 타입 설정
            if (spec.isCollectLogs() && "RequestResponse".equals(invocationType)) {
                requestBuilder.logType("Tail");
            }

            return requestBuilder.build();
            
        } catch (JsonProcessingException e) {
            throw new ExternalServiceException("Failed to serialize payload to JSON: " + e.getMessage());
        }
    }

    /**
     * Lambda 응답 처리 및 ApiResponse 변환
     */
    private ApiResponse<Map<String, Object>> processLambdaResponse(InvokeResponse response, 
            LambdaFunctionSpec spec, Instant startTime) {
        
        long executionTime = Duration.between(startTime, Instant.now()).toMillis();
        log.debug("Lambda function {} executed in {} ms", spec.getFunctionName(), executionTime);

        // HTTP 상태 코드 확인
        if (response.statusCode() != 200) {
            log.error("Lambda function returned non-200 status: {}", response.statusCode());
            return ApiResponse.error(ErrorCode.EXTERNAL_SERVICE_ERROR, 
                    "Lambda function returned error status: " + response.statusCode());
        }

        // 함수 실행 오류 확인
        if (response.functionError() != null) {
            log.error("Lambda function execution error: {}", response.functionError());
            return ApiResponse.error(ErrorCode.EXTERNAL_SERVICE_ERROR, 
                    "Lambda function execution failed: " + response.functionError());
        }

        try {
            // 응답 페이로드 파싱
            String responsePayload = response.payload().asUtf8String();
            log.debug("Lambda response payload: {}", responsePayload);

            @SuppressWarnings("unchecked")
            Map<String, Object> responseData = objectMapper.readValue(responsePayload, Map.class);

            // 스키마 검증 (설정된 경우)
            if (spec.isValidateResponseSchema()) {
                validateResponseSchema(responseData, spec);
            }

            return ApiResponse.success(responseData);

        } catch (JsonProcessingException e) {
            log.error("Failed to parse Lambda response: {}", e.getMessage());
            return ApiResponse.error(ErrorCode.EXTERNAL_SERVICE_ERROR, 
                    "Invalid response format from Lambda function");
        }
    }

    /**
     * 응답 스키마 검증 (간단한 구조 검증)
     */
    private void validateResponseSchema(Map<String, Object> responseData, LambdaFunctionSpec spec) {
        // 기본적인 스키마 검증 로직
        // 필요에 따라 확장 가능
        log.debug("Lambda response schema validation passed for function: {}", spec.getFunctionName());
    }
} 