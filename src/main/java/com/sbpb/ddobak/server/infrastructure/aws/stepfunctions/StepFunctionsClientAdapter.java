package com.sbpb.ddobak.server.infrastructure.aws.stepfunctions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbpb.ddobak.server.common.exception.ErrorCode;
import com.sbpb.ddobak.server.common.exception.ExternalServiceException;
import com.sbpb.ddobak.server.common.response.ApiResponse;
import com.sbpb.ddobak.server.infrastructure.aws.config.AwsInfrastructureConfig.StateMachineSpec;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.HashMap;

/**
 * AWS Step Functions 클라이언트 어댑터
 */
@Slf4j
@Component
@Profile("!test") // 테스트 환경에서는 제외
@RequiredArgsConstructor
public class StepFunctionsClientAdapter implements StepFunctionsInvoker {

    private final SfnClient stepFunctionsClient;
    private final Map<String, StateMachineSpec> productionStateMachines;
    private final ObjectMapper objectMapper;

    /**
     * State Machine 실행 시작 (포트 인터페이스 구현)
     * 
     * @param stateMachineName State Machine 이름
     * @param input 실행 입력 데이터
     * @return ApiResponse 형식의 응답
     */
    @Override
    public ApiResponse<Map<String, Object>> startExecution(String stateMachineName, Map<String, Object> input) {
        log.info("Starting Step Functions execution: {} with input: {}", stateMachineName, input != null);
        
        Instant startTime = Instant.now();
        
        try {
            // State Machine 스펙 조회
            StateMachineSpec spec = getStateMachineSpec(stateMachineName);
            
            // State Machine 실행 시작
            StartExecutionResponse response = startStateMachineExecution(spec, input);
            
            // 응답 처리
            return processStartExecutionResponse(response, spec, startTime);
            
        } catch (SfnException e) {
            log.error("AWS Step Functions service error for state machine {}: {}", stateMachineName, e.getMessage());
            return ApiResponse.error(ErrorCode.EXTERNAL_SERVICE_ERROR, 
                    "Step Functions execution failed: " + e.getMessage());
                    
        } catch (ExternalServiceException e) {
            log.error("Step Functions configuration error: {}", e.getMessage());
            return ApiResponse.error(ErrorCode.EXTERNAL_SERVICE_ERROR, e.getMessage());
            
        } catch (Exception e) {
            log.error("Unexpected error starting Step Functions execution {}: {}", stateMachineName, e.getMessage(), e);
            return ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, 
                    "Unexpected error occurred while starting Step Functions execution");
        }
    }

    /**
     * State Machine 동기 실행 (포트 인터페이스 구현)
     * Express 워크플로우에서만 사용 가능
     * 
     * @param stateMachineName State Machine 이름
     * @param input 실행 입력 데이터
     * @return ApiResponse 형식의 응답 (완료된 결과 포함)
     */
    @Override
    public ApiResponse<Map<String, Object>> startSyncExecution(String stateMachineName, Map<String, Object> input) {
        log.info("Starting Step Functions sync execution: {} with input: {}", stateMachineName, input != null);
        
        Instant startTime = Instant.now();
        
        try {
            // State Machine 스펙 조회
            StateMachineSpec spec = getStateMachineSpec(stateMachineName);
            
            // State Machine 동기 실행 시작
            StartSyncExecutionResponse response = startSyncStateMachineExecution(spec, input);
            
            // 응답 처리
            return processSyncExecutionResponse(response, spec, startTime);
            
        } catch (SfnException e) {
            log.error("AWS Step Functions service error for sync execution {}: {}", stateMachineName, e.getMessage());
            return ApiResponse.error(ErrorCode.EXTERNAL_SERVICE_ERROR, 
                    "Step Functions sync execution failed: " + e.getMessage());
                    
        } catch (ExternalServiceException e) {
            log.error("Step Functions configuration error: {}", e.getMessage());
            return ApiResponse.error(ErrorCode.EXTERNAL_SERVICE_ERROR, e.getMessage());
            
        } catch (Exception e) {
            log.error("Unexpected error starting Step Functions sync execution {}: {}", stateMachineName, e.getMessage(), e);
            return ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, 
                    "Unexpected error occurred while starting Step Functions sync execution");
        }
    }

    /**
     * State Machine 실행 상태 조회 (포트 인터페이스 구현)
     * 
     * @param executionArn 실행 ARN
     * @return ApiResponse 형식의 응답
     */
    @Override
    public ApiResponse<Map<String, Object>> describeExecution(String executionArn) {
        log.info("Describing Step Functions execution: {}", executionArn);
        
        try {
            DescribeExecutionRequest request = DescribeExecutionRequest.builder()
                    .executionArn(executionArn)
                    .build();
                    
            DescribeExecutionResponse response = stepFunctionsClient.describeExecution(request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("executionArn", response.executionArn());
            result.put("status", response.status().toString());
            result.put("startDate", response.startDate().toString());
            result.put("stateMachineArn", response.stateMachineArn());
            
            if (response.stopDate() != null) {
                result.put("stopDate", response.stopDate().toString());
            }
            if (response.output() != null) {
                result.put("output", parseJsonToMap(response.output()));
            }
            if (response.error() != null) {
                result.put("error", response.error());
                result.put("cause", response.cause());
            }
            
            return ApiResponse.success(result);
            
        } catch (SfnException e) {
            log.error("AWS Step Functions service error describing execution {}: {}", executionArn, e.getMessage());
            return ApiResponse.error(ErrorCode.EXTERNAL_SERVICE_ERROR, 
                    "Failed to describe Step Functions execution: " + e.getMessage());
                    
        } catch (Exception e) {
            log.error("Unexpected error describing Step Functions execution {}: {}", executionArn, e.getMessage(), e);
            return ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, 
                    "Unexpected error occurred while describing Step Functions execution");
        }
    }

    /**
     * State Machine 실행 히스토리 조회 (포트 인터페이스 구현)
     * 
     * @param executionArn 실행 ARN
     * @return ApiResponse 형식의 응답
     */
    @Override
    public ApiResponse<Map<String, Object>> getExecutionHistory(String executionArn) {
        log.info("Getting Step Functions execution history: {}", executionArn);
        
        try {
            GetExecutionHistoryRequest request = GetExecutionHistoryRequest.builder()
                    .executionArn(executionArn)
                    .reverseOrder(true) // 최신 이벤트부터
                    .maxResults(100) // 최대 100개 이벤트
                    .build();
                    
            GetExecutionHistoryResponse response = stepFunctionsClient.getExecutionHistory(request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("executionArn", executionArn);
            result.put("events", response.events());
            result.put("eventCount", response.events().size());
            
            return ApiResponse.success(result);
            
        } catch (SfnException e) {
            log.error("AWS Step Functions service error getting execution history {}: {}", executionArn, e.getMessage());
            return ApiResponse.error(ErrorCode.EXTERNAL_SERVICE_ERROR, 
                    "Failed to get Step Functions execution history: " + e.getMessage());
                    
        } catch (Exception e) {
            log.error("Unexpected error getting Step Functions execution history {}: {}", executionArn, e.getMessage(), e);
            return ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, 
                    "Unexpected error occurred while getting Step Functions execution history");
        }
    }

    /**
     * State Machine 실행 중단 (포트 인터페이스 구현)
     * 
     * @param executionArn 실행 ARN
     * @param reason 중단 사유
     * @return ApiResponse 형식의 응답
     */
    @Override
    public ApiResponse<Map<String, Object>> stopExecution(String executionArn, String reason) {
        log.info("Stopping Step Functions execution: {} with reason: {}", executionArn, reason);
        
        try {
            StopExecutionRequest request = StopExecutionRequest.builder()
                    .executionArn(executionArn)
                    .error("USER_REQUESTED_STOP")
                    .cause(reason)
                    .build();
                    
            StopExecutionResponse response = stepFunctionsClient.stopExecution(request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("executionArn", executionArn);
            result.put("stopDate", response.stopDate().toString());
            result.put("reason", reason);
            result.put("status", "STOPPED");
            
            return ApiResponse.success(result);
            
        } catch (SfnException e) {
            log.error("AWS Step Functions service error stopping execution {}: {}", executionArn, e.getMessage());
            return ApiResponse.error(ErrorCode.EXTERNAL_SERVICE_ERROR, 
                    "Failed to stop Step Functions execution: " + e.getMessage());
                    
        } catch (Exception e) {
            log.error("Unexpected error stopping Step Functions execution {}: {}", executionArn, e.getMessage(), e);
            return ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, 
                    "Unexpected error occurred while stopping Step Functions execution");
        }
    }

    // ==================== Private Helper Methods ====================

    /**
     * State Machine 스펙 조회
     */
    private StateMachineSpec getStateMachineSpec(String stateMachineName) {
        StateMachineSpec spec = productionStateMachines.get(stateMachineName);
        if (spec == null) {
            throw new ExternalServiceException("State Machine configuration not found: " + stateMachineName);
        }
        return spec;
    }

    /**
     * State Machine 실행 시작
     */
    private StartExecutionResponse startStateMachineExecution(StateMachineSpec spec, Map<String, Object> input) {
        try {
            StartExecutionRequest.Builder requestBuilder = StartExecutionRequest.builder()
                    .stateMachineArn(spec.getStateMachineArn())
                    .name(generateExecutionName(spec.getName()));

            // 입력 데이터가 있는 경우 추가
            if (input != null) {
                if (spec.isValidateInput()) {
                    validateInput(input, spec);
                }
                String inputJson = objectMapper.writeValueAsString(input);
                requestBuilder.input(inputJson);
            }

            return stepFunctionsClient.startExecution(requestBuilder.build());
            
        } catch (JsonProcessingException e) {
            throw new ExternalServiceException("Failed to serialize input to JSON: " + e.getMessage());
        }
    }

    /**
     * State Machine 동기 실행 시작
     */
    private StartSyncExecutionResponse startSyncStateMachineExecution(StateMachineSpec spec, Map<String, Object> input) {
        try {
            StartSyncExecutionRequest.Builder requestBuilder = StartSyncExecutionRequest.builder()
                    .stateMachineArn(spec.getStateMachineArn())
                    .name(generateExecutionName(spec.getName()));

            // 입력 데이터가 있는 경우 추가
            if (input != null) {
                if (spec.isValidateInput()) {
                    validateInput(input, spec);
                }
                String inputJson = objectMapper.writeValueAsString(input);
                requestBuilder.input(inputJson);
            }

            return stepFunctionsClient.startSyncExecution(requestBuilder.build());
            
        } catch (JsonProcessingException e) {
            throw new ExternalServiceException("Failed to serialize input to JSON: " + e.getMessage());
        }
    }

    /**
     * 실행 시작 응답 처리
     */
    private ApiResponse<Map<String, Object>> processStartExecutionResponse(StartExecutionResponse response, 
            StateMachineSpec spec, Instant startTime) {
        
        long executionTime = Duration.between(startTime, Instant.now()).toMillis();
        log.debug("Step Functions execution started for {} in {} ms", spec.getName(), executionTime);

        Map<String, Object> result = new HashMap<>();
        result.put("executionArn", response.executionArn());
        result.put("startDate", response.startDate().toString());
        result.put("stateMachineName", spec.getName());
        result.put("status", "RUNNING");

        return ApiResponse.success(result);
    }

    /**
     * 동기 실행 응답 처리
     */
    private ApiResponse<Map<String, Object>> processSyncExecutionResponse(StartSyncExecutionResponse response, 
            StateMachineSpec spec, Instant startTime) {
        
        long executionTime = Duration.between(startTime, Instant.now()).toMillis();
        log.info("Step Functions sync execution completed for {} in {} ms", spec.getName(), executionTime);

        Map<String, Object> result = new HashMap<>();
        result.put("executionArn", response.executionArn());
        result.put("startDate", response.startDate().toString());
        result.put("stopDate", response.stopDate().toString());
        result.put("stateMachineName", spec.getName());
        result.put("status", response.status().toString());
        result.put("executionTime", executionTime);

        // 실행 결과 출력이 있는 경우 파싱하여 포함
        if (response.output() != null && !response.output().isEmpty()) {
            Map<String, Object> outputData = parseJsonToMap(response.output());
            result.put("output", outputData);
            log.debug("Sync execution output parsed successfully for {}", spec.getName());
        }

        // 에러가 있는 경우 포함
        if (response.error() != null) {
            result.put("error", response.error());
            result.put("cause", response.cause());
            log.warn("Sync execution completed with error for {}: {}", spec.getName(), response.error());
        }

        return ApiResponse.success(result);
    }

    /**
     * 실행 이름 생성 (고유한 이름 생성)
     */
    private String generateExecutionName(String baseName) {
        return baseName + "-" + System.currentTimeMillis();
    }

    /**
     * 입력 데이터 검증
     */
    private void validateInput(Map<String, Object> input, StateMachineSpec spec) {
        // 기본적인 입력 검증 로직
        // 필요에 따라 확장 가능
        if (input.isEmpty()) {
            log.warn("Empty input provided for state machine: {}", spec.getName());
        }
        log.debug("Input validation passed for state machine: {}", spec.getName());
    }

    /**
     * JSON 문자열을 Map으로 변환
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonToMap(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse JSON to Map: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("rawOutput", json);
            return result;
        }
    }
} 