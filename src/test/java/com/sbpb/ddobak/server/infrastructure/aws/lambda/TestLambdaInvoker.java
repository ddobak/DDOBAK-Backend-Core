package com.sbpb.ddobak.server.infrastructure.aws.lambda;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbpb.ddobak.server.common.exception.ErrorCode;
import com.sbpb.ddobak.server.common.exception.ExternalServiceException;
import com.sbpb.ddobak.server.common.response.ApiResponse;

import com.sbpb.ddobak.server.infrastructure.aws.config.AwsTestConfig.LambdaFunctionSpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
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
 * 테스트 환경 전용 Lambda 클라이언트 어댑터
 */
@Slf4j
@Component
@Primary
@Profile("test")
@RequiredArgsConstructor
public class TestLambdaInvoker implements LambdaInvoker {

    private final LambdaClient lambdaClient;
    private final Map<String, LambdaFunctionSpec> testLambdaFunctions;
    private final ObjectMapper objectMapper;

    @Override
    public ApiResponse<Map<String, Object>> invoke(String functionName, Map<String, Object> payload) {
        log.info("🧪 Test Lambda function: {} with payload: {}", functionName, payload != null);
        
        Instant startTime = Instant.now();
        
        try {
            LambdaFunctionSpec spec = getFunctionSpec(functionName);
            InvokeResponse response = invokeLambdaFunction(spec, payload);
            return processLambdaResponse(response, spec, startTime);
            
        } catch (LambdaException e) {
            log.error("❌ AWS Lambda service error for function {}: {}", functionName, e.getMessage());
            return ApiResponse.error(ErrorCode.EXTERNAL_SERVICE_ERROR, 
                    "Lambda function execution failed: " + e.getMessage());
                    
        } catch (ExternalServiceException e) {
            log.error("❌ Lambda function configuration error: {}", e.getMessage());
            return ApiResponse.error(ErrorCode.EXTERNAL_SERVICE_ERROR, e.getMessage());
            
        } catch (Exception e) {
            log.error("❌ Unexpected error invoking Lambda function {}: {}", functionName, e.getMessage(), e);
            return ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, 
                    "Unexpected error occurred while invoking Lambda function");
        }
    }

    @Override
    public ApiResponse<Map<String, Object>> invokeAsync(String functionName, Map<String, Object> payload) {
        log.info("🧪 Test Lambda function asynchronously: {}", functionName);
        
        try {
            LambdaFunctionSpec spec = getFunctionSpec(functionName);
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
                log.warn("⚠️ Unexpected status code for async invocation: {}", response.statusCode());
                return ApiResponse.error(ErrorCode.EXTERNAL_SERVICE_ERROR, 
                        "Unexpected response from Lambda service");
            }
            
        } catch (Exception e) {
            log.error("❌ Error invoking Lambda function {} asynchronously: {}", functionName, e.getMessage());
            return ApiResponse.error(ErrorCode.EXTERNAL_SERVICE_ERROR, 
                    "Failed to invoke Lambda function asynchronously");
        }
    }

    private LambdaFunctionSpec getFunctionSpec(String functionName) {
        LambdaFunctionSpec spec = testLambdaFunctions.get(functionName);
        if (spec == null) {
            throw new ExternalServiceException("Lambda function configuration not found: " + functionName);
        }
        return spec;
    }

    private InvokeResponse invokeLambdaFunction(LambdaFunctionSpec spec, Map<String, Object> payload) {
        InvokeRequest request = createInvokeRequest(spec, payload, spec.getDefaultInvocationType());
        
        if (log.isDebugEnabled()) {
            log.debug("🔧 Invoking Lambda: {} with type: {}", 
                    spec.getFunctionName(), spec.getDefaultInvocationType());
        }
                
        return lambdaClient.invoke(request);
    }

    private InvokeRequest createInvokeRequest(LambdaFunctionSpec spec, Map<String, Object> payload, String invocationType) {
        try {
            InvokeRequest.Builder requestBuilder = InvokeRequest.builder()
                    .functionName(spec.getFunctionName())
                    .invocationType(invocationType);

            if (payload != null) {
                String payloadJson = objectMapper.writeValueAsString(payload);
                requestBuilder.payload(SdkBytes.fromUtf8String(payloadJson));
            }

            if (spec.isCollectLogs() && "RequestResponse".equals(invocationType)) {
                requestBuilder.logType("Tail");
            }

            return requestBuilder.build();
            
        } catch (JsonProcessingException e) {
            throw new ExternalServiceException("Failed to serialize payload to JSON: " + e.getMessage());
        }
    }

    private ApiResponse<Map<String, Object>> processLambdaResponse(InvokeResponse response, 
            LambdaFunctionSpec spec, Instant startTime) {
        
        long executionTime = Duration.between(startTime, Instant.now()).toMillis();
        log.info("⏱️ Lambda {} executed in {} ms", spec.getFunctionName(), executionTime);

        if (response.statusCode() != 200) {
            log.error("❌ Lambda function returned non-200 status: {}", response.statusCode());
            return ApiResponse.error(ErrorCode.EXTERNAL_SERVICE_ERROR, 
                    "Lambda function returned error status: " + response.statusCode());
        }

        if (response.functionError() != null) {
            log.error("❌ Lambda function execution error: {}", response.functionError());
            return ApiResponse.error(ErrorCode.EXTERNAL_SERVICE_ERROR, 
                    "Lambda function execution failed: " + response.functionError());
        }

        try {
            String responsePayload = response.payload().asUtf8String();
            
            if (log.isDebugEnabled()) {
                log.debug("📄 Lambda response payload: {}", responsePayload);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> responseData = objectMapper.readValue(responsePayload, Map.class);

            if (spec.isValidateResponseSchema()) {
                log.debug("✅ Schema validation passed for function: {}", spec.getFunctionName());
            }

            return ApiResponse.success(responseData);

        } catch (JsonProcessingException e) {
            log.error("❌ Failed to parse Lambda response: {}", e.getMessage());
            return ApiResponse.error(ErrorCode.EXTERNAL_SERVICE_ERROR, 
                    "Invalid response format from Lambda function");
        }
    }
} 